package app.notekea.chat.data

import app.notekea.chat.domain.ChatHistoryStore
import app.notekea.chat.domain.ChatTurn
import app.notekea.core.domain.AppDispatchers
import co.touchlab.kermit.Logger
import com.ghost.serialization.Ghost
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/**
 * [ChatHistoryStore] as one JSON file per vault in [folder], named by a hash of the vault's path, written whole to a
 * file beside it and moved in place, so a crash never leaves half a history; writes go one at a time, as two at once
 * shared the partial file. A file that cannot be read is an empty history: the chat goes on.
 */
class FileChatHistoryStore(
    private val folder: Path,
    private val dispatchers: AppDispatchers
) : ChatHistoryStore {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val writing = Mutex()

    override suspend fun load(vaultPath: String): List<ChatTurn> =
        withContext(context = dispatchers.io) {
            val file = fileOf(vaultPath = vaultPath)
            if (!Files.isRegularFile(file)) return@withContext emptyList()

            try {
                val dto = Ghost.deserialize<ChatHistoryDto>(Files.readString(file))

                ChatHistoryMappers.toDomain(dto = dto)
            } catch (exception: IOException) {
                logger.w(throwable = exception) { READ_FAILED_MESSAGE }
                emptyList()
            } catch (exception: RuntimeException) {
                // A file written by another version or damaged: its turns are lost, not the chat.
                logger.w(throwable = exception) { READ_FAILED_MESSAGE }
                emptyList()
            }
        }

    override suspend fun save(
        vaultPath: String,
        turns: List<ChatTurn>
    ) = withContext(context = dispatchers.io) {
        writing.withLock {
            write(
                vaultPath = vaultPath,
                turns = turns
            )
        }
    }

    private fun write(
        vaultPath: String,
        turns: List<ChatTurn>
    ) {
        val file = fileOf(vaultPath = vaultPath)
        val json = Ghost.encodeToString(ChatHistoryMappers.toDto(turns = turns))

        try {
            Files.createDirectories(folder)

            val written =
                Files.writeString(folder.resolve(file.fileName.toString() + PARTIAL_SUFFIX), json)

            Files.move(
                written,
                file,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (exception: IOException) {
            logger.w(throwable = exception) { WRITE_FAILED_MESSAGE }
        }
    }

    override suspend fun clear(vaultPath: String) = withContext(context = dispatchers.io) {
        writing.withLock {
            try {
                Files.deleteIfExists(fileOf(vaultPath = vaultPath))
            } catch (exception: IOException) {
                logger.w(throwable = exception) { WRITE_FAILED_MESSAGE }
            }
        }

        Unit
    }

    private fun fileOf(vaultPath: String): Path {
        val hash = MessageDigest
            .getInstance(SHA_256)
            .digest(vaultPath.toByteArray(charset = Charsets.UTF_8))
            .joinToString(separator = "") { "%02x".format(it) }

        return folder.resolve("$hash$JSON_SUFFIX")
    }

    private companion object {
        const val SHA_256 = "SHA-256"
        const val JSON_SUFFIX = ".json"
        const val PARTIAL_SUFFIX = ".part"
        const val LOG_TAG = "FileChatHistoryStore"
        const val READ_FAILED_MESSAGE = "A chat history could not be read"
        const val WRITE_FAILED_MESSAGE = "A chat history could not be written"
    }
}

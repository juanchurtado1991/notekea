package app.notekea.models.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.models.domain.ModelError
import app.notekea.models.domain.ModelFile
import app.notekea.models.domain.ModelStorage
import co.touchlab.kermit.Logger
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/** [ModelStorage] on the files of [modelFolder], with the SHA-256 of the JDK. */
class NioModelStorage(
    private val modelFolder: ModelFolder,
    private val dispatchers: AppDispatchers
) : ModelStorage {
    private val logger = Logger.withTag(tag = LOG_TAG)

    override val folder: String get() = modelFolder.root.toString()

    override suspend fun isInstalled(
        file: ModelFile
    ): Boolean = withContext(context = dispatchers.io) {
        val path = modelFolder.installed(file = file)
        Files.isRegularFile(path) && Files.size(path) == file.sizeBytes
    }

    override suspend fun partialBytes(
        file: ModelFile
    ): Long = withContext(context = dispatchers.io) {
        val path = modelFolder.partial(file = file)
        if (Files.isRegularFile(path)) Files.size(path) else 0L
    }

    override suspend fun freeBytes(): Long = withContext(context = dispatchers.io) {
        Files.createDirectories(modelFolder.root)
        Files.getFileStore(modelFolder.root).usableSpace
    }

    override suspend fun verifyAndInstall(
        file: ModelFile
    ): Outcome<Unit, ModelError> = withContext(context = dispatchers.io) {
        val partial = modelFolder.partial(file = file)
        try {
            if (sha256Of(file = file) != file.sha256) {
                logger.w { "Checksum mismatch for ${file.fileName}; deleted" }
                Files.deleteIfExists(partial)
                Outcome.Failure(error = ModelError.ChecksumMismatch)
            } else {
                Files.move(
                    partial,
                    modelFolder.installed(file = file),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
                Outcome.Success(value = Unit)
            }
        } catch (exception: IOException) {
            logger.w(throwable = exception) { "Could not install ${file.fileName}" }
            Outcome.Failure(error = ModelError.Storage)
        }
    }

    override suspend fun delete(
        file: ModelFile
    ) {
        withContext(context = dispatchers.io) {
            Files.deleteIfExists(modelFolder.installed(file = file))
            Files.deleteIfExists(modelFolder.partial(file = file))
        }
    }

    private suspend fun sha256Of(
        file: ModelFile
    ): String {
        val digest = MessageDigest.getInstance(SHA_256)
        val buffer = ByteArray(BUFFER_BYTES)
        Files.newInputStream(modelFolder.partial(file = file)).use { input ->
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                currentCoroutineContext().ensureActive()
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { HEX_FORMAT.format(it) }
    }

    private companion object {
        const val BUFFER_BYTES = 1 shl 20
        const val HEX_FORMAT = "%02x"
        const val LOG_TAG = "NioModelStorage"
        const val SHA_256 = "SHA-256"
    }
}

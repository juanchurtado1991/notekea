package app.notekea.chat.eval

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger

/**
 * The replies of an evaluation's model kept on disk under what decides each (the model's file, the messages, how it is
 * written), so a round made again asks the model only what changed. Never for a number that stands alone: a kept reply
 * hides that a GPU gives another one now and then (`docs/gemma-plan.md`, section 7).
 */
internal class ReplyCache(
    private val engine: LlmEngine,
    private val folder: Path,
    private val model: String
) : LlmEngine by engine {
    private val kept = AtomicInteger()
    private val asked = AtomicInteger()

    /** How many replies were read from disk, and how many the model wrote. */
    val counts: String get() = "replies kept ${kept.get()}, asked ${asked.get()}"

    override fun generate(
        messages: List<ChatMessage>,
        options: GenerationOptions
    ): Flow<String> = flow {
        val key = keyOf(
            messages = messages,
            options = options
        )

        val file = folder.resolve(key)

        if (Files.isRegularFile(file)) {
            kept.incrementAndGet()
            emit(value = Files.readString(file))
        } else {
            val written = StringBuilder()

            engine
                .generate(
                    messages = messages,
                    options = options
                )
                .collect {
                    written.append(it)
                    emit(value = it)
                }

            // Only a reply written to its end is kept: one cut by an error or a cancel never reaches here.
            asked.incrementAndGet()

            keep(
                file = file,
                reply = written.toString()
            )
        }
    }

    /** Writes [counts] at the end of the folder's [ROUNDS], a line a round, for the script that measures. */
    fun note() {
        Files.createDirectories(folder)

        Files.writeString(
            folder.resolve(ROUNDS),
            "$counts\n",
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        )
    }

    // Written beside and moved, so a task running side by side never reads half a reply.
    private fun keep(
        file: Path,
        reply: String
    ) {
        Files.createDirectories(folder)
        val draft = Files.createTempFile(folder, DRAFT, null)
        Files.writeString(draft, reply)
        Files.move(draft, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun keyOf(
        messages: List<ChatMessage>,
        options: GenerationOptions
    ): String {
        val written = listOf(
            model,
            options.maxTokens,
            options.temperature,
            options.seed,
            options.jsonSchema
        )

        val parts = written + messages.flatMap { listOf(it.role.name, it.text) }
        val digest = MessageDigest.getInstance(HASH)

        // Each part with its length before it, so two calls never read alike by where their parts are cut.
        parts.forEach {
            val bytes = it.toString().toByteArray()
            digest.update("${bytes.size}:".toByteArray())
            digest.update(bytes)
        }

        return digest
            .digest()
            .joinToString(separator = "") { BYTE.format(it) }
    }

    companion object {
        /** The folder Gradle passes as `-PchatReplies`; empty, the default, keeps nothing. */
        const val PROPERTY = "notekea.chat.replies"

        /** The file of the folder that says, a line a round, how many replies were kept and asked. */
        const val ROUNDS = "rounds.txt"
        private const val HASH = "SHA-256"
        private const val BYTE = "%02x"
        private const val DRAFT = "draft"

        /**
         * [engine] with its replies kept in the folder of [PROPERTY], or [engine] itself when none is given. The
         * model is told by the file [modelPath] leads to and its length, as a file may stand under another's name.
         */
        fun around(
            engine: LlmEngine,
            modelPath: String
        ): LlmEngine {
            val folder = System.getProperty(PROPERTY).orEmpty()
            val file = Path.of(modelPath)
            val isKept = folder.isNotEmpty() && Files.isRegularFile(file)
            if (!isKept) return engine

            val real = file.toRealPath()

            return ReplyCache(
                engine = engine,
                folder = Path.of(folder),
                model = "${real.fileName}:${Files.size(real)}"
            )
        }
    }
}

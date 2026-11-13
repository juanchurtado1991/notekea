package app.notekea.core.embedding.data

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.TextEmbedder
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import net.ladenthin.llama.LlamaModel
import net.ladenthin.llama.parameters.ModelParameters
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * [TextEmbedder] on java-llama.cpp with the bundled model at [modelPath]. The model loads on the first call and is let
 * go after [idleMillis] without one; calls run one at a time on the model's own thread, which keeps glibc from piling
 * up freed memory per thread (`docs/performance.md`). The native library is extracted to [nativeFolder], not `/tmp`.
 */
class LlamaTextEmbedder(
    private val modelPath: String,
    private val nativeFolder: String,
    private val idleMillis: Long = IDLE_MILLIS
) : TextEmbedder,
    AutoCloseable {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val mutex = Mutex()
    private val modelThread = Executors.newSingleThreadExecutor {
        Thread(it, THREAD_NAME).apply { isDaemon = true }
    }
        .asCoroutineDispatcher()
    private val scope = CoroutineScope(context = SupervisorJob() + modelThread)

    @Volatile
    private var model: LlamaModel? = null
    private var release: Job? = null

    // The engine's version too: a new llama.cpp changes the vectors slightly, so the notes are indexed again.
    override val modelId: String = "$MODEL_FILE/$ENGINE_VERSION"

    override val dimensions: Int = DIMENSIONS

    override val maxTokens: Int = CONTEXT_TOKENS - SPECIAL_TOKENS

    /** Whether the model is in memory now. */
    val isLoaded: Boolean get() = model != null

    /** Whether it was closed. */
    val isClosed: Boolean get() = !scope.isActive

    override suspend fun embed(texts: List<String>): Outcome<List<FloatArray>, EmbeddingError> =
        withContext(
            context = modelThread
        ) {
            mutex.withLock {
                release?.cancel()

                try {
                    embedAll(texts = texts)
                } finally {
                    releaseWhenIdle()
                }
            }
        }

    /** Stops the idle timer and lets the model go, waiting a moment for a call still running. */
    override fun close() {
        freed(within = CLOSE_WAIT)
    }

    /**
     * Lets the model go for good, waiting up to [within] for a call still running; false when it still held the
     * model, which must not be there when the process ends (`LlamaLlmEngine.freed`).
     */
    fun freed(within: Duration): Boolean {
        scope.cancel()

        val isGone = runBlocking {
            withTimeoutOrNull(timeout = within) {
                mutex.withLock { unload() }

                true
            }
        } ?: false

        if (isGone) modelThread.close()

        return isGone
    }

    private fun releaseWhenIdle() {
        if (isClosed) return unload()

        release = scope.launch {
            delay(timeMillis = idleMillis)

            mutex.withLock { unload() }
        }
    }

    private suspend fun embedAll(texts: List<String>): Outcome<List<FloatArray>, EmbeddingError> =
        try {
            val loaded = model ?: load()
                ?: return Outcome.Failure(error = EmbeddingError.ModelMissing(path = modelPath))

            val vectors = ArrayList<FloatArray>(texts.size)

            texts.forEachIndexed { index, text ->
                coroutineContext.ensureActive()
                val tokens = loaded.encode(text).size

                val tooLong = EmbeddingError.TextTooLong(
                    index = index,
                    tokens = tokens
                )

                if (tokens > maxTokens) return Outcome.Failure(error = tooLong)
                vectors += normalized(vector = loaded.embed(text))
            }

            Outcome.Success(value = vectors)
        } catch (cancellation: CancellationException) {
            // A cancelled call (the vault closed, a new run) is not the model failing.
            throw cancellation
        } catch (exception: RuntimeException) {
            // java-llama.cpp's LlamaException and its kin: a model that fails to load or to embed.
            logger.e(throwable = exception) { ENGINE_FAILED_MESSAGE }
            Outcome.Failure(error = EmbeddingError.EngineFailed)
        } catch (error: LinkageError) {
            // The native library does not load on this system: search goes on with keywords.
            logger.e(throwable = error) { ENGINE_FAILED_MESSAGE }
            Outcome.Failure(error = EmbeddingError.EngineFailed)
        }

    private fun load(): LlamaModel? {
        if (!Files.isRegularFile(Path.of(modelPath))) return null

        System.setProperty(
            NATIVE_FOLDER_PROPERTY,
            Files.createDirectories(Path.of(nativeFolder)).toString()
        )

        val parameters = ModelParameters()
            .setModel(modelPath)
            .enableEmbedding()
            // On the CPU always: the vectors stay the ones indexed, and the GPU is left to the chat model.
            .setGpuLayers(0)
            // No GPU device at all: llama.cpp otherwise keeps one in use with no layer on it, and its buffers too.
            .setDevices(NO_DEVICES)
            .setCtxSize(CONTEXT_TOKENS)
            .setBatchSize(CONTEXT_TOKENS)
            .setUbatchSize(CONTEXT_TOKENS)
            .setLogVerbosity(ERRORS_ONLY)

        return LlamaModel(parameters).also { model = it }
    }

    private fun unload() {
        model?.close()
        model = null
    }

    /** [vector] scaled to length 1, so a dot product is a cosine similarity. */
    private fun normalized(vector: FloatArray): FloatArray {
        val length = sqrt(vector.sumOf { (it * it).toDouble() }).toFloat()

        return FloatArray(size = vector.size) {
            vector[it] / length
        }
    }

    companion object {
        /** The bundled model's file, in the app's resources' `models` folder. */
        const val MODEL_FILE = "granite-embedding-107m-multilingual-Q8_0.gguf"
        const val MODEL_FOLDER = "models"

        private const val ENGINE_VERSION = "llama.cpp-b11968"
        private const val CONTEXT_TOKENS = 512
        private const val NO_DEVICES = "none"
        private val CLOSE_WAIT = 2.seconds
        private const val DIMENSIONS = 384

        // The model adds a token at each end of every text, inside its context.
        private const val SPECIAL_TOKENS = 2
        private const val IDLE_MILLIS = 3 * 60_000L

        // llama.cpp's default level logs every text it embeds: the notes' words stay out of the logs.
        private const val ERRORS_ONLY = 1
        private const val NATIVE_FOLDER_PROPERTY = "net.ladenthin.llama.tmpdir"
        private const val THREAD_NAME = "notekea-embedding-model"
        private const val LOG_TAG = "LlamaTextEmbedder"
        private const val ENGINE_FAILED_MESSAGE = "The embedding model failed"
    }
}

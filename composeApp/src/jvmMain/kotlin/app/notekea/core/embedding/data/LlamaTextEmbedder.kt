package app.notekea.core.embedding.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.TextEmbedder
import co.touchlab.kermit.Logger
import de.kherud.llama.LlamaModel
import de.kherud.llama.ModelParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt

/**
 * [TextEmbedder] on java-llama.cpp with the bundled multilingual model at [modelPath] (`ROADMAP.md` 2.2,
 * `docs/spikes/2026-11-01-embedding-models.md`). The model loads on the first call and is let go after [idleMillis]
 * without one, so writing notes never pays for its memory; calls run one at a time on the default dispatcher and a
 * cancelled one stops between texts. java-llama.cpp extracts its native library to [nativeFolder] (the app's data),
 * not `/tmp`, which some systems mount without the right to run it.
 */
class LlamaTextEmbedder(
    private val modelPath: String,
    private val nativeFolder: String,
    private val dispatchers: AppDispatchers,
    private val idleMillis: Long = IDLE_MILLIS
) : TextEmbedder {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val mutex = Mutex()
    private val scope = CoroutineScope(context = SupervisorJob() + dispatchers.default)
    private var model: LlamaModel? = null
    private var release: Job? = null

    override val modelId: String = MODEL_FILE

    override val dimensions: Int = DIMENSIONS

    override val maxTokens: Int = CONTEXT_TOKENS - SPECIAL_TOKENS

    /** Whether the model is in memory now. */
    val isLoaded: Boolean get() = model != null

    override suspend fun embed(
        texts: List<String>
    ): Outcome<List<FloatArray>, EmbeddingError> = withContext(context = dispatchers.default) {
        mutex.withLock {
            release?.cancel()
            val outcome = embedAll(texts = texts)
            release = scope.launch {
                delay(timeMillis = idleMillis)
                mutex.withLock { unload() }
            }
            outcome
        }
    }

    private suspend fun embedAll(
        texts: List<String>
    ): Outcome<List<FloatArray>, EmbeddingError> = try {
        val loaded = model ?: load() ?: return Outcome.Failure(error = EmbeddingError.ModelMissing(path = modelPath))
        val vectors = ArrayList<FloatArray>(texts.size)
        texts.forEachIndexed { index, text ->
            coroutineContext.ensureActive()
            val tokens = loaded.encode(text).size
            val tooLong = EmbeddingError.TextTooLong(index = index, tokens = tokens)
            if (tokens > maxTokens) return Outcome.Failure(error = tooLong)
            vectors += normalized(vector = loaded.embed(text))
        }
        Outcome.Success(value = vectors)
    } catch (exception: RuntimeException) {
        // java-llama.cpp's LlamaException (a RuntimeException) is not public: a model that fails to load or to embed.
        logger.e(throwable = exception) { ENGINE_FAILED_MESSAGE }
        Outcome.Failure(error = EmbeddingError.EngineFailed)
    }

    private fun load(): LlamaModel? {
        if (!Files.isRegularFile(Path.of(modelPath))) return null
        System.setProperty(NATIVE_FOLDER_PROPERTY, Files.createDirectories(Path.of(nativeFolder)).toString())
        val parameters = ModelParameters()
            .setModel(modelPath)
            .enableEmbedding()
            .setCtxSize(CONTEXT_TOKENS)
            .setBatchSize(CONTEXT_TOKENS)
            .setUbatchSize(CONTEXT_TOKENS)
        return LlamaModel(parameters).also { model = it }
    }

    private fun unload() {
        model?.close()
        model = null
    }

    /** [vector] scaled to length 1, so a dot product is a cosine similarity. */
    private fun normalized(
        vector: FloatArray
    ): FloatArray {
        val length = sqrt(vector.sumOf { (it * it).toDouble() }).toFloat()
        return FloatArray(size = vector.size) { vector[it] / length }
    }

    companion object {
        /** The bundled model's file, in the app's resources' `models` folder. */
        const val MODEL_FILE = "granite-embedding-107m-multilingual-Q8_0.gguf"
        const val MODEL_FOLDER = "models"

        private const val CONTEXT_TOKENS = 512
        private const val DIMENSIONS = 384

        // The model adds a token at each end of every text, inside its context.
        private const val SPECIAL_TOKENS = 2
        private const val IDLE_MILLIS = 3 * 60_000L
        private const val NATIVE_FOLDER_PROPERTY = "de.kherud.llama.tmpdir"
        private const val LOG_TAG = "LlamaTextEmbedder"
        private const val ENGINE_FAILED_MESSAGE = "The embedding model failed"
    }
}

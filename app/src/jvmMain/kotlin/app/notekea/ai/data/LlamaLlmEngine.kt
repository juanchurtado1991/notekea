package app.notekea.ai.data

import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSetupSource
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.llm.domain.LlmException
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import net.ladenthin.llama.LlamaModel
import net.ladenthin.llama.args.LoadMode
import net.ladenthin.llama.parameters.InferenceParameters
import net.ladenthin.llama.parameters.ModelParameters
import java.nio.file.Files
import java.nio.file.Path
import kotlin.coroutines.coroutineContext
import net.ladenthin.llama.value.ChatMessage as LlamaMessage

/**
 * [LlmEngine] on java-llama.cpp: the model in use ([setupSource]) and the reranker load on the first call and leave
 * after [idleMillis] with no reply running. On the CPU the weights are read without mmap (3.0: 24–31 % less memory),
 * on the GPU every layer is there; the native library goes to [nativeFolder], not `/tmp`.
 */
class LlamaLlmEngine(
    private val setupSource: ModelSetupSource,
    private val nativeFolder: String,
    private val dispatchers: AppDispatchers,
    private val idleMillis: Long = IDLE_MILLIS
) : LlmEngine,
    AutoCloseable {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val mutex = Mutex()
    private val scope = CoroutineScope(context = SupervisorJob() + dispatchers.io)
    private var loaded: LoadedLlama? = null
    private var users = 0
    private var release: Job? = null

    /** Whether a model is in memory now. */
    val isLoaded: Boolean get() = loaded != null

    override fun generate(
        messages: List<ChatMessage>,
        options: GenerationOptions
    ): Flow<String> = channelFlow {
        withModels { models ->
            withContext(context = dispatchers.io) {
                val reply = models.chat.generateChat(
                    parametersOf(
                        messages = messages,
                        options = options
                    )
                )

                val pieces = reply.iterator()

                try {
                    while (pieces.hasNext()) {
                        coroutineContext.ensureActive()
                        val text = pieces.next().text
                        if (text.isNotEmpty()) send(element = text)
                    }
                } finally {
                    // A cancelled collector stops the model instead of letting it write to the end.
                    pieces.cancel()
                    reply.close()
                }
            }
        }
    }

    override suspend fun rerank(
        query: String,
        texts: List<String>
    ): Outcome<List<Float>, LlmError> = try {
        withModels { models ->
            withContext(context = dispatchers.io) {
                val json = models.reranker.handleRerank(query, *texts.toTypedArray())

                Outcome.Success(
                    value = RerankScores.parse(
                        json = json,
                        count = texts.size
                    )
                )
            }
        }
    } catch (exception: LlmException) {
        Outcome.Failure(error = exception.error)
    }

    /** Lets the model go now; a reply still running keeps it until it ends. */
    override fun close() {
        scope.cancel()

        if (mutex.tryLock()) {
            try {
                if (users == 0) unload()
            } finally {
                mutex.unlock()
            }
        }
    }

    /** Runs [block] with the models loaded and a slot of their own; fails with [LlmException] when they cannot load. */
    private suspend fun <T> withModels(block: suspend (LoadedLlama) -> T): T {
        val models = mutex.withLock {
            release?.cancel()
            users++

            try {
                current()
            } catch (exception: Throwable) {
                users--
                releaseWhenIdle()
                throw exception
            }
        }

        try {
            return models.slots.withPermit { block(models) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: LlmException) {
            throw exception
        } catch (exception: RuntimeException) {
            // java-llama.cpp's LlamaException and its kin: the model failed while it wrote.
            logger.e(throwable = exception) { ENGINE_FAILED_MESSAGE }
            throw LlmException(error = LlmError.EngineFailed)
        } finally {
            mutex.withLock {
                users--
                releaseWhenIdle()
            }
        }
    }

    /** The models of the setup in use, loaded now if needed; the ones loaded stay while other replies run. */
    private suspend fun current(): LoadedLlama {
        val setup = when (val found = setupSource.current()) {
            is Outcome.Success -> found.value
            is Outcome.Failure -> throw LlmException(error = found.error)
        }

        val models = loaded
        if (models != null && (models.setup == setup || users > 1)) return models
        unload()

        return withContext(context = dispatchers.io) { load(setup = setup) }.also { loaded = it }
    }

    private fun load(setup: ModelSetup): LoadedLlama {
        System.setProperty(
            NATIVE_FOLDER_PROPERTY,
            Files.createDirectories(Path.of(nativeFolder)).toString()
        )

        return try {
            val chat = LlamaModel(chatParameters(setup = setup))

            val reranker = try {
                LlamaModel(rerankerParameters(setup = setup))
            } catch (exception: RuntimeException) {
                chat.close()
                throw exception
            }

            LoadedLlama(
                setup = setup,
                chat = chat,
                reranker = reranker
            )
        } catch (exception: RuntimeException) {
            logger.e(throwable = exception) { LOAD_FAILED_MESSAGE }
            throw LlmException(error = LlmError.EngineFailed)
        } catch (error: LinkageError) {
            // The native library does not load on this system.
            logger.e(throwable = error) { LOAD_FAILED_MESSAGE }
            throw LlmException(error = LlmError.EngineFailed)
        }
    }

    private fun releaseWhenIdle() {
        if (users > 0) return
        if (!scope.isActive) return unload()

        release = scope.launch {
            delay(timeMillis = idleMillis)

            mutex.withLock {
                if (users == 0) unload()
            }
        }
    }

    private fun unload() {
        loaded?.close()
        loaded = null
    }

    companion object {
        /** Five minutes without a reply, then the model leaves memory (3.2). */
        const val IDLE_MILLIS = 5 * 60_000L

        private const val ALL_LAYERS = -1
        private const val RERANKER_CONTEXT = 4_096

        // llama.cpp's default level logs every prompt: the notes' words stay out of the logs.
        private const val ERRORS_ONLY = 1
        private const val NATIVE_FOLDER_PROPERTY = "net.ladenthin.llama.tmpdir"
        private const val THINKING_KEY = "enable_thinking"
        private const val THINKING_OFF = "false"
        private const val LOG_TAG = "LlamaLlmEngine"
        private const val LOAD_FAILED_MESSAGE = "The chat model failed to load"
        private const val ENGINE_FAILED_MESSAGE = "The chat model failed"

        /** Each slot keeps its own cache (3.0), and the CPU reads the weights without mmap. */
        private fun chatParameters(setup: ModelSetup): ModelParameters = ModelParameters()
            .setModel(setup.modelPath)
            .setParallel(setup.slots)
            .setCtxSize(setup.contextTokens * setup.slots)
            .setKvUnified(false)
            .setGpuLayers(if (setup.onGpu) ALL_LAYERS else 0)
            .setLoadMode(if (setup.onGpu) LoadMode.AUTO else LoadMode.NONE)
            .setLogVerbosity(ERRORS_ONLY)

        private fun rerankerParameters(setup: ModelSetup): ModelParameters = ModelParameters()
            .setModel(setup.rerankerPath)
            .enableReranking()
            .setCtxSize(RERANKER_CONTEXT)
            .setBatchSize(RERANKER_CONTEXT)
            .setUbatchSize(RERANKER_CONTEXT)
            .setGpuLayers(if (setup.onGpu) ALL_LAYERS else 0)
            .setLoadMode(if (setup.onGpu) LoadMode.AUTO else LoadMode.NONE)
            .setLogVerbosity(ERRORS_ONLY)

        /** Qwen3.5 thinks before it answers unless told not to: the answers here need no thinking (3.0). */
        private fun parametersOf(
            messages: List<ChatMessage>,
            options: GenerationOptions
        ): InferenceParameters {
            val parameters = InferenceParameters("")
                .withMessages(
                    messages.map {
                        LlamaMessage(
                            it
                                .role
                                .name
                                .lowercase(),
                            it.text
                        )
                    }
                )
                .withNPredict(options.maxTokens)
                .withTemperature(options.temperature)
                .withSeed(options.seed)
                .withChatTemplateKwargs(mapOf(THINKING_KEY to THINKING_OFF))
                .withReasoningBudgetTokens(0)

            return options.jsonSchema?.let { parameters.withJsonSchema(it) } ?: parameters
        }
    }
}

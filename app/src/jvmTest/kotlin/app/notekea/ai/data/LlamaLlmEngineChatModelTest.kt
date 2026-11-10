package app.notekea.ai.data

import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.llm.domain.LlmException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assume.assumeTrue
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * The Small chat model and the reranker of the catalog through java-llama.cpp, on the GPU when Vulkan finds one: run by
 * hand with `./gradlew :app:chatModelTest -PchatModels=<folder>` (`app/build.gradle.kts`), skipped without the files.
 */
class LlamaLlmEngineChatModelTest {
    private val folder = Path.of(System.getProperty(MODELS_PROPERTY).orEmpty())
    private val natives = Files.createTempDirectory("notekea-natives").toString()
    private val dispatchers = object : AppDispatchers {
        override val main = Dispatchers.Default
        override val default = Dispatchers.Default
        override val io = Dispatchers.IO
    }

    private var engine: LlamaLlmEngine? = null

    @AfterTest
    fun tearDown() {
        engine?.close()
    }

    @Test
    fun `a reply streams in pieces, and the same seed writes the same reply`() = runBlocking {
        val engine = engine()

        val first = engine.generate(messages = question(text = QUESTION)).toList()
        val second = engine.generate(messages = question(text = QUESTION)).toList()

        assertTrue(
            actual = first.size > 1,
            message = "$first"
        )

        assertEquals(
            expected = first.joinToString(separator = ""),
            actual = second.joinToString(separator = "")
        )

        assertTrue(actual = "agua" in first.joinToString(separator = "").lowercase())
    }

    @Test
    fun `the reranker scores in the order of the texts, the one that answers highest`() =
        runBlocking {
            val scores = (
                engine().rerank(
                    query = QUESTION,
                    texts = listOf(UNRELATED, RECIPE, UNRELATED)
                ) as Outcome.Success
                ).value

            assertEquals(
                expected = 3,
                actual = scores.size
            )

            assertTrue(
                actual = scores[1] > scores[0] &&
                    scores[1] > scores[2] &&
                    scores[0] == scores[2],
                message = "$scores"
            )
        }

    @Test
    fun `a reply cancelled stops the model, and replies side by side all end`() = runBlocking {
        val engine = engine()

        val few = engine
            .generate(
                messages = question(text = "Write a long story about bread."),
                options = GenerationOptions(maxTokens = 400)
            )
            .take(count = 3)
            .toList()

        val together = withTimeout(timeMillis = WAIT_MILLIS) {
            List(size = 4) {
                async {
                    engine
                        .generate(
                            messages = question(text = QUESTION),
                            options = GenerationOptions(seed = it)
                        )
                        .toList()
                        .joinToString(separator = "")
                }
            }.awaitAll()
        }

        assertEquals(
            expected = listOf(3, 4),
            actual = listOf(few.size, together.count { it.isNotBlank() })
        )
    }

    @Test
    fun `a JSON schema keeps the reply valid JSON of that shape`() = runBlocking {
        val reply = engine()
            .generate(
                messages = question(text = "Give two search queries for: arepa dough water."),
                options = GenerationOptions(
                    maxTokens = 120,
                    jsonSchema = """{"type":"object","properties":{"queries":{"type":"array",""" +
                        """"items":{"type":"string"},"maxItems":3}},"required":["queries"]}"""
                )
            )
            .toList()
            .joinToString(separator = "")
            .trim()

        assertTrue(
            actual = reply.startsWith(prefix = "{\"queries\"") && reply.endsWith(suffix = "}"),
            message = reply
        )
    }

    @Test
    fun `the model leaves memory when idle, and a missing file fails as the engine failing`() =
        runBlocking {
            val engine = engine(idleMillis = 200)
            engine.generate(messages = question(text = "Hola")).toList()

            withTimeout(timeMillis = WAIT_MILLIS) {
                while (engine.isLoaded) delay(timeMillis = 50)
            }

            val missing = engine(modelFile = "missing.gguf")

            val error = assertFailsWith<LlmException> {
                missing.generate(messages = question(text = "Hola")).toList()
            }

            assertEquals(
                expected = LlmError.EngineFailed,
                actual = error.error
            )
        }

    private fun engine(
        modelFile: String = SMALL_FILE,
        idleMillis: Long = LlamaLlmEngine.IDLE_MILLIS
    ): LlamaLlmEngine {
        val model = folder.resolve(modelFile)
        val reranker = folder.resolve(RERANKER_FILE)
        assumeTrue(Files.isRegularFile(folder.resolve(SMALL_FILE)) && Files.isRegularFile(reranker))

        val setup = ModelSetup(
            size = ModelSize.SMALL,
            modelPath = model.toString(),
            rerankerPath = reranker.toString(),
            slots = 4,
            contextTokens = ModelSetup.CONTEXT_TOKENS,
            onGpu = true
        )

        return LlamaLlmEngine(
            setupSource = { Outcome.Success(value = setup) },
            nativeFolder = natives,
            dispatchers = dispatchers,
            idleMillis = idleMillis
        ).also {
            engine?.close()
            engine = it
        }
    }

    private fun question(text: String): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.USER,
            text = text
        )
    )

    private companion object {
        const val MODELS_PROPERTY = "notekea.chat.models"
        const val SMALL_FILE = "Qwen3.5-2B-Q4_K_M.gguf"
        const val RERANKER_FILE = "bge-reranker-v2-m3-Q4_K_M.gguf"
        const val WAIT_MILLIS = 120_000L
        const val QUESTION = "¿Cuánta agua lleva la masa de las arepas? Responde en una frase."
        const val RECIPE =
            "Arepas: two cups of pre-cooked corn flour and two and a half cups of warm water."
        const val UNRELATED = "Quartz counters, six weeks. The quote goes into the budget."
    }
}

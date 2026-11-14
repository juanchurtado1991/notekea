package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test

/**
 * Records [ActionFixtures]: every request of the evaluation planned by the Medium model, one at a time on one
 * slot, with each reply kept as it came. Run by hand when the planner's prompt changes:
 * `./gradlew :app:chatModelTest -PchatModels=<folder> --tests '*ActionFixturesChatModelTest*'`.
 */
class ActionFixturesChatModelTest {
    private val folder = Path.of(System.getProperty("notekea.chat.models").orEmpty())

    private val setup = ModelSetup(
        size = ModelSize.MEDIUM,
        modelPath = folder.resolve("Qwen3.5-4B-Q4_K_M.gguf").toString(),
        rerankerPath = folder.resolve("bge-reranker-v2-m3-Q4_K_M.gguf").toString(),
        slots = 1,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = true
    )

    @Test
    fun `the model's replies to the evaluation requests are recorded`() {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        val engine = LlamaLlmEngine(
            setupSource = { Outcome.Success(value = setup) },
            nativeFolder = Files.createTempDirectory("notekea-natives").toString(),
            dispatchers = JvmAppDispatchers()
        )

        val replies = mutableListOf<String>()

        val recording = object : LlmEngine by engine {
            override fun generate(
                messages: List<ChatMessage>,
                options: GenerationOptions
            ): Flow<String> = flow {
                val reply = StringBuilder()

                engine
                    .generate(
                        messages = messages,
                        options = options
                    )
                    .collect {
                        reply.append(it)
                        emit(value = it)
                    }

                replies += reply.toString()
            }
        }

        val fixtures = ActionFixtures.requests().map { request ->
            replies.clear()

            val isRight = runBlocking {
                ActionFixtures.isRight(
                    request = request,
                    engine = recording
                )
            }

            ActionFixtures.fixture(
                request = request,
                replies = replies.toList(),
                isRight = isRight
            )
        }

        engine.close()
        ActionFixtures.written(fixtures = fixtures)
        println("${fixtures.count { it.get(ActionFixtures.RIGHT).asBoolean() }} of ${fixtures.size} right")
    }
}

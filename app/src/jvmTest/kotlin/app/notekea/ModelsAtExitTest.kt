package app.notekea

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.llm.domain.LlmException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

/**
 * The models are let go before the process ends (the crash report at quit after a chat, code review of 2026-11-13):
 * with nothing still using them no halt is needed, and an engine closed for good takes no new reply.
 */
class ModelsAtExitTest {
    private val engine = LlamaLlmEngine(
        setupSource = { Outcome.Failure(error = LlmError.NoModel) },
        nativeFolder = System.getProperty("java.io.tmpdir"),
        dispatchers = JvmAppDispatchers()
    )

    @Test
    fun `models nobody uses leave at once with no halt, and the closed engine refuses a new reply`() =
        runTest {
            var halts = 0

            ModelsAtExit.release(
                engine = engine,
                embedder = null,
                within = 1.seconds,
                halt = {
                    halts++
                }
            )

            assertFailsWith<LlmException> {
                engine
                    .generate(
                        messages = listOf(
                            ChatMessage(
                                role = ChatRole.USER,
                                text = "hello"
                            )
                        )
                    )
                    .toList()
            }

            assertEquals(
                expected = listOf<Any>(0, false),
                actual = listOf(halts, engine.isLoaded)
            )
        }
}

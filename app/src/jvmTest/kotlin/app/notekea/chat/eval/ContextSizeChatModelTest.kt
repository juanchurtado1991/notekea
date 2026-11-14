package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test

/**
 * How much the chat model can read in one call, for the one agent loop (`docs/agent-loop.md`, step 0): a document
 * of numbered clauses of growing length is asked about its first and last clause, with the context split among
 * 4, 2 or 1 slots. A tool to read, not a pass or fail: it writes `build/reports/context-size.md`.
 */
class ContextSizeChatModelTest {
    private val folder = Path.of(System.getProperty("notekea.chat.models").orEmpty())
    private val model = folder.resolve("Qwen3.5-4B-Q4_K_M.gguf").toString()

    @Test
    fun `the model is asked about documents of growing length, with the context split in fewer slots`() {
        assumeTrue(Files.isRegularFile(Path.of(model)))

        val report =
            StringBuilder("# Context size (medium)\n\n| slots | tokens a slot | clauses | ")

        report.append("characters | seconds | resident MB | reply |\n|---|---|---|---|---|---|---|\n")

        SETUPS.forEach { (slots, context) ->
            CLAUSES.forEach { clauses ->
                report.append(
                    row(
                        slots = slots,
                        context = context,
                        clauses = clauses
                    )
                )
            }
        }

        File("build/reports/context-size.md").run {
            parentFile.mkdirs()
            writeText(text = report.toString())
        }
    }

    private fun row(
        slots: Int,
        context: Int,
        clauses: Int
    ): String {
        val engine = LlamaLlmEngine(
            setupSource = {
                Outcome.Success(
                    value = ModelSetup(
                        size = ModelSize.MEDIUM,
                        modelPath = model,
                        rerankerPath = folder.resolve("bge-reranker-v2-m3-Q4_K_M.gguf").toString(),
                        slots = slots,
                        contextTokens = context,
                        onGpu = true
                    )
                )
            },
            nativeFolder = Files.createTempDirectory("notekea-natives").toString(),
            dispatchers = JvmAppDispatchers()
        )

        val document = (1..clauses)
            .joinToString(separator = "\n") {
                "Clause $it: the fee for service $it is ${FEE + it} dollars a month."
            }

        val start = System.nanoTime()

        val reply = try {
            runBlocking {
                engine
                    .generate(
                        messages = listOf(
                            ChatMessage(
                                role = ChatRole.SYSTEM,
                                text = "Answer from the document only, in one short sentence."
                            ),
                            ChatMessage(
                                role = ChatRole.USER,
                                text = "$document\n\nWhat is the fee for service 2 and for service $clauses?"
                            )
                        ),
                        options = GenerationOptions(
                            maxTokens = REPLY_TOKENS,
                            temperature = 0f
                        )
                    )
                    .toList()
                    .joinToString(separator = "")
                    .trim()
                    .replace(
                        oldChar = '\n',
                        newChar = ' '
                    )
            }
        } catch (exception: LlmException) {
            "FAILED: ${exception.error}"
        }

        val seconds = (System.nanoTime() - start) / NANOS
        val resident = residentMegabytes()
        engine.close()

        return "| $slots | $context | $clauses | ${document.length} | $seconds | $resident | $reply |\n"
    }

    private fun residentMegabytes(): Long = ProcessBuilder(
        "ps",
        "-o",
        "rss=",
        "-p",
        ProcessHandle
            .current()
            .pid()
            .toString()
    )
        .start()
        .inputStream
        .bufferedReader()
        .readText()
        .trim()
        .toLongOrNull()
        ?.div(other = KILOBYTES) ?: 0

    private companion object {
        // The same memory for the context each time (16,384 tokens), split among fewer slots.
        val SETUPS = listOf(4 to 4_096, 2 to 8_192, 1 to 16_384)
        val CLAUSES = listOf(100, 220, 450, 900)
        const val FEE = 1_000
        const val REPLY_TOKENS = 60
        const val NANOS = 1_000_000_000L
        const val KILOBYTES = 1_024L
    }
}

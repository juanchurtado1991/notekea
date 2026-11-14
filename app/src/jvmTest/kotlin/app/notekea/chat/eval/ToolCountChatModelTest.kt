package app.notekea.chat.eval

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assume.assumeTrue
import java.io.File
import kotlin.test.Test

/**
 * Step 0 of `docs/agent-loop.md`: how many tools the Medium model chooses well among. The requests whose action is
 * one of the first five tools are asked with 5, 10, 20 and all 33 tools on offer, in both formats. A tool to
 * read, not a pass or fail: it writes `build/reports/tool-count.md`.
 */
class ToolCountChatModelTest {
    @Test
    fun `the same requests are asked with more and more tools on offer`() {
        assumeTrue(RawChatModel.isInstalled)

        val base = EvalTools
            .ALL
            .take(n = COUNTS.first())
            .map { it.name }

        val requests = ObjectMapper()
            .readTree(File(SET_FILE))
            .filter {
                it.at("/steps/0/action").asText() in base
            }

        val report = StringBuilder("# How many tools (medium)\n\n| tools, format ${ArmRun.HEADER}")

        report.append("|---|---|---|---|---|---|---|\n")

        RawChatModel(contextTokens = CONTEXT_TOKENS).use { model ->
            COUNTS.forEach { count ->
                val tools = EvalTools.ALL.take(n = count)

                listOf(ToolArm.ours(tools = tools), ToolArm.native(tools = tools)).forEach { arm ->
                    val run = ArmRun.of(
                        arm = arm,
                        model = model,
                        requests = requests
                    )

                    report.append(run.line(label = "$count, ${arm.name}"))
                    File(REPORTS, "tool-count.md").writeText(text = report.toString())
                }
            }
        }
    }

    private companion object {
        const val SET_FILE = "../smoke-data/eval/actions.json"
        const val REPORTS = "build/reports"
        const val CONTEXT_TOKENS = 8_192

        // The first five tools of the catalog are those the most requests of the set ask for.
        val COUNTS = listOf(5, 10, 20, 33)
    }
}

package app.notekea.chat.eval

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assume.assumeTrue
import java.io.File
import kotlin.test.Test

/**
 * Step 0 of `docs/agent-loop.md`: the requests of `smoke-data/eval/actions.json` asked of the Medium model in each
 * format a tool call may take, graded on the first step as it comes, with none of the planner's repairs. A tool
 * to read, not a pass or fail: it writes `build/reports/tool-format.md` and a `.tsv` of each format's replies.
 */
class ToolFormatChatModelTest {
    @Test
    fun `the evaluation requests are asked in each format of a tool call`() {
        assumeTrue(RawChatModel.isInstalled)

        val requests = ObjectMapper().readTree(File(SET_FILE)).toList()
        val report = StringBuilder("# Formats of a tool call (medium)\n\n| format ${ArmRun.HEADER}")

        report.append("|---|---|---|---|---|---|---|\n")

        RawChatModel(contextTokens = CONTEXT_TOKENS).use { model ->
            ARMS.forEachIndexed { index, arm ->
                val run = ArmRun.of(
                    arm = arm,
                    model = model,
                    requests = requests
                )

                report.append(run.line())
                run.written(file = File(REPORTS, "tool-format-${index + 1}.tsv"))
                File(REPORTS, "tool-format.md").writeText(text = report.toString())
            }
        }
    }

    private companion object {
        const val SET_FILE = "../smoke-data/eval/actions.json"
        const val REPORTS = "build/reports"

        // One slot wide enough for the tools written out.
        const val CONTEXT_TOKENS = 12_288

        val ARMS = listOf(
            ToolArm.ours(tools = EvalTools.ALL),
            ToolArm.native(tools = EvalTools.ALL)
        )
    }
}

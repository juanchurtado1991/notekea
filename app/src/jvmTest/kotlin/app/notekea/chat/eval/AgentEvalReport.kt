package app.notekea.chat.eval

import app.notekea.ai.domain.ModelSize
import app.notekea.chat.domain.agent.AgentStep

/** The report of one evaluation set (`build/reports/<set>-<size>.md`): the share right, and each task's calls. */
internal class AgentEvalReport(
    private val trace: EvalTrace,
    private val size: ModelSize
) {
    fun of(
        vault: String,
        results: List<AgentResult>,
        right: Int
    ): String = buildString {
        val moves = results.map {
            it.steps.count { step ->
                step is AgentStep.Moving
            }
        }

        append("# Agent mode on $vault (${size.id})\n\n")
        append("$right of ${results.size} right (${right * PERCENT / results.size} %), ")
        append("moves ${moves.average().toInt()} on average (${moves.max()} at most), ")
        val seconds = results.map { it.seconds }

        append("${seconds.average().toInt()} s a task on average (${seconds.max()} s at most)\n")

        val categories = results.mapNotNull { result ->
            result.problem?.let {
                EvalTraceReport.categoryOf(
                    problem = it,
                    end = result.steps.last(),
                    calls = trace.of(task = result.case.task)
                )
            }
        }

        append(EvalTraceReport.summary(categories = categories))

        results.forEach { result ->
            val calls = trace.of(task = result.case.task)

            append("\n## ${if (result.problem == null) "✓" else "✗"} ${result.case.task}\n\n")

            EvalTraceReport.routeOf(calls = calls)?.let { append("Routed to $it.\n\n") }

            trace
                .frameOf(task = result.case.task)
                ?.let { append("Frame: ${EvalTraceReport.frameOf(frame = it)}.\n\n") }

            result.steps.forEach { append("- ${shown(step = it)}\n") }

            result.problem?.let {
                append(
                    failed(
                        result = result,
                        problem = it
                    )
                )
            }

            append("\n${result.seconds} s\n")
        }
    }

    private fun shown(step: AgentStep): String = when (step) {
        is AgentStep.Moving -> "${step.move} ${step.target}"
        else -> step.toString().take(n = STEP_CHARS)
    }

    // What went wrong, with what the agent had found by its last move.
    private fun failed(
        result: AgentResult,
        problem: String
    ): String = buildString {
        val calls = trace.of(task = result.case.task)
        val category = EvalTraceReport.categoryOf(problem, result.steps.last(), calls)

        append("\n**$problem**\n\nKind: $category.\n")

        trace.lastPrompt(task = result.case.task)?.let { prompt ->
            append("\n<details><summary>Found by the last move</summary>\n\n```\n")
            append(prompt.substringAfter(delimiter = "so far:\n").take(n = PROMPT_CHARS))
            append("\n```\n</details>\n")
        }

        append(EvalTraceReport.traced(calls = calls))
    }

    private companion object {
        const val PERCENT = 100
        const val PROMPT_CHARS = 4_000
        const val STEP_CHARS = 600
    }
}

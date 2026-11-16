package app.notekea.chat.eval

import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.tool.web.WebSearchTool

/**
 * The web evaluation's report: each task with its rubric, every turn the person took with what they asked, its
 * searches and pages, and the whole end, for a person to grade. Only what the run can tell is counted here.
 */
internal object WebEvalReport {
    fun of(
        size: String,
        results: List<WebEvalResult>,
        trace: EvalTrace
    ): String = buildString {
        val ended = results.count {
            it.end is AgentStep.Finished ||
                it.end is AgentStep.Proposed ||
                it.end is AgentStep.Asked
        }

        val searched = results.count { result ->
            result.steps.any {
                it is AgentStep.Moving && it.move == WebSearchTool.NAME
            }
        }

        append("# The web's tasks ($size)\n\n")
        append("$ended of ${results.size} ended with an answer, a proposal or a question; ")
        append("${results.count { it.turns.size > 1 }} went on to another turn; $searched searched the web; ")
        append("${results.map { it.seconds }.average().toInt()} s a task on average.\n")

        results.forEach { result ->
            append("\n## ${result.case.kind}: ${result.case.task}\n\n")
            append("Rubric: ${result.case.rubric}\n\n")

            result.turns.forEachIndexed { index, turn ->
                val asked = turn.asked.prependIndent(indent = "    ")
                if (index > 0) append("\nThe person then asked:\n\n$asked\n\n")
                turn.steps.forEach { append("- ${shown(step = it)}\n") }
            }

            val isStopped = result.end is AgentStep.Stopped

            if (isStopped) {
                append(
                    EvalTraceReport.traced(
                        calls = trace.of(
                            task = result
                                .turns
                                .last()
                                .asked
                        )
                    )
                )
            }

            append("\n${result.seconds} s\n")
        }
    }

    // A move by its tool and target; the end whole, since it is what is graded.
    private fun shown(step: AgentStep): String = when (step) {
        is AgentStep.Moving -> "${step.move} ${step.target}"
        is AgentStep.Finished -> "Finished:\n\n${step.summary.prependIndent(indent = "  > ")}\n"
        else -> step.toString()
    }
}

package app.notekea.chat.domain.tool.work

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult

/**
 * The plan of a task of several steps, written by the model and ticked as it goes: the list the person sees, and
 * what keeps a long task on its way (`docs/agent-loop.md`, step 4).
 */
class PlanTool : ChatTool {
    override val name = NAME

    override val description =
        "for a task of several steps: the steps, in order, each starting with [x] once done. Send them all " +
            "again as you go."

    override val arguments = listOf(
        ToolArgument(
            name = STEPS,
            isList = true
        )
    )

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = stepsOf(call = call).joinToString(separator = "; ") { it.text }

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val steps = stepsOf(call = call)
        if (steps.isEmpty()) return ToolResult(forModel = "plan: write its steps.")

        context.work.plan(steps = steps)
        val done = steps.count { it.isDone }

        val next = steps.firstOrNull {
            !it.isDone
        }?.text

        return ToolResult(
            forModel = "plan kept, $done of ${steps.size} done." +
                next
                    ?.let { " Next: $it" }
                    .orEmpty()
        )
    }

    private fun stepsOf(call: ToolCall): List<PlanStep> = call
        .list(name = STEPS)
        .take(n = MAX_STEPS)
        .map { line ->
            val text = DONE.replace(
                input = line,
                replacement = ""
            )

            PlanStep(
                text = OPEN
                    .replace(
                        input = text,
                        replacement = ""
                    )
                    .trim(),
                isDone = text != line
            )
        }
        .filter { it.text.isNotEmpty() }

    companion object {
        const val NAME = "plan"
        const val STEPS = "steps"
        private const val MAX_STEPS = 12
        private val DONE = Regex(pattern = "^\\s*(-\\s*)?\\[[xX]]\\s*")
        private val OPEN = Regex(pattern = "^\\s*(-\\s*)?(\\[\\s?]\\s*)?")
    }
}

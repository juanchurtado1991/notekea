package app.notekea.chat.domain.tool.work

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult

/**
 * A fact the turn found, kept with the passage that says it: it stays in the prompt when the result it came from is
 * trimmed, and an answer written from it can be cited (`docs/agent-loop.md`, step 4).
 */
class NoteTool : ChatTool {
    override val name = NAME

    override val description =
        "keep a fact you found for later in this task, with the passage that says it, copied as written."

    override val arguments = listOf(
        ToolArgument(name = FACT),
        ToolArgument(name = PASSAGE)
    )

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.text(name = FACT)

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val fact = call.text(name = FACT)
        val passage = call.text(name = PASSAGE)
        val isMissing = fact.isEmpty() || passage.isEmpty()

        if (isMissing) {
            return ToolResult(forModel = "note: write the fact and the passage that says it.")
        }

        context.work.note(
            fact = NotedFact(
                fact = fact,
                passage = passage.take(n = PASSAGE_CHARS)
            )
        )

        return ToolResult(forModel = "noted: $fact")
    }

    companion object {
        const val NAME = "note"
        const val FACT = "fact"
        const val PASSAGE = "passage"
        private const val PASSAGE_CHARS = 300
    }
}

package app.notekea.chat.domain.tool.work

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult

/**
 * A question to the person, with the answers to choose from as buttons, in place of a guess: when the request can
 * be read two ways or something it needs is missing. It ends the turn (`docs/agent-loop.md`, step 4).
 */
class AskUserTool : ChatTool {
    override val name = NAME

    override val description =
        "when the request can be read two ways or something needed is missing: one short question to the " +
            "person, and two to four answers to choose from; answers that are notes are written as the vault " +
            "names them (Home/Budget 2026). Do not guess."

    override val arguments = listOf(
        ToolArgument(name = QUESTION),
        ToolArgument(
            name = OPTIONS,
            isList = true
        )
    )

    override val effect = ToolEffect.ENDS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.text(name = QUESTION)

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val question = call.text(name = QUESTION)
        if (question.isEmpty()) return ToolResult(forModel = "ask_user: write the question.")

        val options = optionsOf(call = call)

        return ToolResult(
            forModel = "asked the person: $question" +
                options
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(
                        separator = " | ",
                        prefix = " ["
                    )
                    ?.plus(other = "]")
                    .orEmpty()
        )
    }

    companion object {
        const val NAME = "ask_user"
        const val QUESTION = "question"
        const val OPTIONS = "options"
        private const val MAX_OPTIONS = 4

        /** The answers [call] offers, each once, four at most. */
        fun optionsOf(call: ToolCall): List<String> = call
            .list(name = OPTIONS)
            .distinct()
            .take(n = MAX_OPTIONS)
    }
}

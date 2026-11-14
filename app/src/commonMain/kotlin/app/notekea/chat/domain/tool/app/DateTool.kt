package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult

/**
 * The day a phrase of the request names, counted from today by the app: a task due "next Friday" gets Friday's
 * date, not the model's guess of it (`docs/agent-loop.md`, step 4).
 */
class DateTool : ChatTool {
    override val name = NAME

    override val description =
        "a phrase that names a day, as tomorrow, next Friday, in 3 weeks or end of month; you get its date. " +
            "Never work a date out yourself."

    override val arguments = listOf(ToolArgument(name = PHRASE))
    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.text(name = PHRASE)

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val phrase = call.text(name = PHRASE)

        val today =
            context.today ?: return ToolResult(forModel = "date: today's date is not known here.")

        val date = DatePhrases.of(
            phrase = phrase,
            today = today
        )

        val now = DatePhrases.written(date = today)

        return if (date == null) {
            ToolResult(forModel = "date \"$phrase\": I cannot tell which day that is. Today is $now.")
        } else {
            ToolResult(forModel = "date \"$phrase\": ${DatePhrases.written(date = date)}. Today is $now.")
        }
    }

    companion object {
        const val NAME = "date"
        const val PHRASE = "phrase"
    }
}

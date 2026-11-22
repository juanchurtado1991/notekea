package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.chat.domain.tool.app.DatePhrases
import app.notekea.core.domain.time.CalendarDate

/** The vault's open tasks with when each is due and the day it writes; only those due between two days, if given. */
class TasksTool(actionRunner: ActionRunner) : ChatTool {
    private val foundRows = FoundRows(actionRunner = actionRunner)

    override val name = NAME

    override val description =
        "the open tasks of the vault, each with when it is due and its date: look here for what is late, " +
            "overdue, due or next. from and to (2027-01-01), both optional, keep only the tasks dated between them."

    override val arguments = listOf(
        ToolArgument(
            name = FROM,
            isRequired = false
        ),
        ToolArgument(
            name = TO,
            isRequired = false
        )
    )

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = listOf(call.text(name = FROM), call.text(name = TO))
        .filter { it.isNotEmpty() }
        .joinToString(separator = " to ")

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val written = listOf(call.text(name = FROM), call.text(name = TO))

        val days = written.map {
            dayOf(
                written = it,
                context = context
            )
        }

        // The code review of 2026-11-22: a day that was not read ("next Friday") was left out
        // without a word, and every task came back.
        val unread = written.filterIndexed { at, day ->
            day.isNotEmpty() && days[at] == null
        }

        if (unread.isNotEmpty()) {
            return ToolResult(
                forModel = "tasks: I cannot tell which day ${unread.joinToString()} is. Write it as 2026-12-06."
            )
        }

        val (from, to) = days

        return foundRows.of(
            step = ActionStep(kind = ActionKind.SHOW_TASKS),
            context = context,
            keep = { row ->
                val isFrom = from == null || row.date?.let { it >= from } == true
                val isTo = to == null || row.date?.let { it <= to } == true

                isFrom && isTo
            }
        )
    }

    // A day as the date tool reads it: written whole, or a phrase counted from today ("tomorrow").
    private fun dayOf(
        written: String,
        context: ToolContext
    ): CalendarDate? = written
        .takeIf { it.isNotEmpty() }
        ?.let {
            DatePhrases.of(
                phrase = it,
                today = context.today
            )
        }

    companion object {
        const val NAME = "tasks"
        private const val FROM = "from"
        private const val TO = "to"
    }
}

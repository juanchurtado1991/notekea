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
        val from = dateOf(written = call.text(name = FROM))
        val to = dateOf(written = call.text(name = TO))

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

    private fun dateOf(written: String): CalendarDate? = ISO
        .find(input = written)
        ?.destructured
        ?.let { (year, month, day) ->
            CalendarDate(
                year = year.toInt(),
                month = month.toInt(),
                day = day.toInt()
            ).takeIf { it.isValid }
        }

    companion object {
        const val NAME = "tasks"
        private const val FROM = "from"
        private const val TO = "to"
        private val ISO = Regex(pattern = "(\\d{4})-(\\d{1,2})-(\\d{1,2})")
    }
}

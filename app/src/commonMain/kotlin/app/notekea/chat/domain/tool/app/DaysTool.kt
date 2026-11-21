package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.domain.time.CalendarDate

/**
 * The days from one day to another, counted by the app (`docs/agent-figures.md`): "how many days until the trip",
 * "how long was I off running". Each day is a date as the notes write it (2026-12-06) or a phrase [DatePhrases] reads
 * (today, next Friday). `calculate` could not count them, and the model counted them wrong.
 */
class DaysTool : ChatTool {
    override val name = NAME

    override val description =
        "the days between two days: from and to, each a date (2026-12-06) or a phrase (today, next Friday); you " +
            "get how many days apart they are. Never count days yourself."

    override val arguments = listOf(
        ToolArgument(name = FROM),
        ToolArgument(name = TO)
    )

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = "${call.text(name = FROM)} to ${call.text(name = TO)}"

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val today =
            context.today ?: return ToolResult(forModel = "days: today's date is not known here.")

        val from = call.text(name = FROM)
        val to = call.text(name = TO)

        val start = DatePhrases.of(
            phrase = from,
            today = today
        )

        val end = DatePhrases.of(
            phrase = to,
            today = today
        )

        if (start == null || end == null) {
            val unread = listOfNotNull(
                from.takeIf {
                    start == null
                },
                to.takeIf {
                    end == null
                }
            ).joinToString()

            return ToolResult(forModel = "days: I cannot tell which day $unread is. Write it as 2026-12-06.")
        }

        val days = end.epochDay - start.epochDay
        val shown = "${DatePhrases.written(date = start)} to ${DatePhrases.written(date = end)}"

        return ToolResult(forModel = "days: $days days from $shown.${yearlyOf(start = start, end = end)}")
    }

    // A day of a past year from a later one is mostly a day that comes back each year, a birthday or an anniversary:
    // "cuántos días faltan para el cumple de Andrés" gave his birth date, 1983, and got -15692 days (the loop's dev
    // exams, 2026-11-21). Its next return is told beside the count, and the model takes the one the task asks.
    private fun yearlyOf(
        start: CalendarDate,
        end: CalendarDate
    ): String {
        if (end.year >= start.year) return ""

        val next = listOf(start.year, start.year + 1)
            .map {
                CalendarDate(
                    year = it,
                    month = end.month,
                    day = end.day
                )
            }
            .firstOrNull {
                it.isValid && it >= start
            } ?: return ""

        val left = next.epochDay - start.epochDay

        return " If it comes back each year (a birthday, an anniversary), the next is " +
            "${DatePhrases.written(date = next)}, $left days from ${DatePhrases.written(date = start)}."
    }

    companion object {
        const val NAME = "days"
        const val FROM = "from"
        const val TO = "to"
    }
}

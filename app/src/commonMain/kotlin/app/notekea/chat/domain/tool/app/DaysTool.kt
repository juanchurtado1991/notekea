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
        "the days between two days: ${FigureReason.TEXT}, from and to, each a date (2026-12-06) or a phrase (today, next Friday); you " +
            "get how many days apart they are. Never count days yourself."

    override val arguments = listOf(
        ToolArgument(
            name = FigureReason.WHY,
            isReason = true
        ),
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

        // The answer is written from this alone: it says which day today is, and when a day of an earlier year next
        // comes round. "Days until the ledger cutover" was answered "the notes do not hold today's date" (dev,
        // 2026-11-25), and a birthday written with its year of birth was counted from that year.
        val next = listOf(start, end)
            .filter {
                it.year < today.year
            }
            .joinToString(separator = "") {
                roundOf(
                    date = it,
                    today = today
                )
            }

        val said = "Today is ${DatePhrases.written(date = today)}.$next"

        return ToolResult(forModel = "days: $days days from $shown. $said")
    }

    // When [date] next comes round from [today]: its month and day this year, or the next. A 29 February falls on 1
    // March in a year with none.
    private fun roundOf(
        date: CalendarDate,
        today: CalendarDate
    ): String {
        val next = listOf(today.year, today.year + 1)
            .map { year ->
                val same = CalendarDate(
                    year = year,
                    month = date.month,
                    day = date.day
                )

                if (same.isValid) {
                    same
                } else {
                    CalendarDate(
                        year = year,
                        month = date.month + 1,
                        day = 1
                    )
                }
            }
            .first {
                it >= today
            }

        val days = next.epochDay - today.epochDay

        return " ${date.iso} comes round again on ${DatePhrases.written(date = next)}, in $days days."
    }

    companion object {
        const val NAME = "days"
        const val FROM = "from"
        const val TO = "to"
    }
}

package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.app.CalculateTool
import app.notekea.chat.domain.tool.app.CountTool
import app.notekea.chat.domain.tool.app.DaysTool
import app.notekea.chat.domain.tool.app.FigureValues
import app.notekea.chat.domain.tool.app.RowsTool
import app.notekea.chat.domain.tool.app.TallyTool
import app.notekea.chat.domain.tool.work.AnswerTool

/**
 * An answer giving a figure (two digits or more) that no tool gave and the task does not write, a sum the model did
 * itself or made up (`docs/agent-figures.md`): held back once, to work it out with a tool or leave it out.
 */
internal object AnswerFigures {
    fun heldOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        if (call.name != AnswerTool.NAME) return null

        val backed = (state.found + state.task)
            .flatMap { FigureValues.allIn(text = it) }

        val unbacked = FIGURE
            .findAll(input = call.text(name = AnswerTool.TEXT))
            .map { it.value.trimEnd('.', ',') }
            .filter { written ->
                written.count { it.isDigit() } >= MIN_DIGITS
            }
            .filter { written ->
                val value = FigureValues.valueOf(written = written) ?: return@filter false

                backed.none {
                    FigureValues.isSame(
                        one = it,
                        other = value
                    )
                }
            }
            .distinct()
            .toList()

        val isHeld = unbacked.isNotEmpty() && state.told.add(element = TOLD)
        if (!isHeld) return null

        // The next call may only work a figure out: told to, the 4B gave the same answer again (the dev run of
        // 2026-11-20: "the most words with an outlet" answered twice with sums it did itself).
        state.narrowed += FIGURE_TOOLS

        return "(Not done: the answer says ${unbacked.joinToString()}, which no tool gave. Work each figure out with " +
            "rows and tally, calculate or days from what you read; then answer with it, or leave it out.)"
    }

    private val FIGURE_TOOLS = listOf(
        RowsTool.NAME,
        TallyTool.NAME,
        CalculateTool.NAME,
        DaysTool.NAME,
        CountTool.NAME
    )

    private val FIGURE = Regex(pattern = "\\d[\\d.,]*")
    private const val MIN_DIGITS = 2
    private const val TOLD = "answer figures"
}

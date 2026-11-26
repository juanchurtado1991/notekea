package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.domain.figures.FigureValues

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

        return "(Not done: the answer says ${unbacked.joinToString()}, which no tool gave. Work each figure out with " +
            "tally, calculate or days from the lines you read, or leave it out.)"
    }

    private val FIGURE = Regex(pattern = "\\d[\\d.,]*")
    private const val MIN_DIGITS = 2
    private const val TOLD = "answer figures"
}

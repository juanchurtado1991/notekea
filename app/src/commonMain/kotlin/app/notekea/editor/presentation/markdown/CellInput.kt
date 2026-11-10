package app.notekea.editor.presentation.markdown

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.TextRange
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import kotlin.math.max
import kotlin.math.min

/**
 * What is typed in a table's cell ([isInCell]) stays in it: a line break is written as a space and a pipe is escaped,
 * or they would end the row or the cell; and a space stays between the text and the cell's closing pipe.
 */
@OptIn(ExperimentalFoundationApi::class)
internal class CellInput(private val isInCell: () -> Boolean) : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val isTyped = changes.changeCount > 0 && selection.collapsed

        if (!isTyped || !isInCell()) return

        var start = length
        var end = 0

        for (change in 0 until changes.changeCount) {
            val range = changes.getRange(changeIndex = change)

            start = min(
                a = start,
                b = range.min
            )

            end = max(
                a = end,
                b = range.max
            )
        }

        var cursor = selection.end

        for (index in end - 1 downTo start) {
            val typed = charAt(index = index)
            val isBarePipe = typed == PIPE && (index == 0 || charAt(index = index - 1) != ESCAPE)

            if (typed == NEW_LINE_CHAR) {
                replace(
                    start = index,
                    end = index + 1,
                    text = SPACE
                )
            } else if (isBarePipe) {
                replace(
                    start = index,
                    end = index,
                    text = ESCAPE.toString()
                )

                if (index < cursor) cursor++
            }
        }

        val isAtPipe = cursor < length && charAt(index = cursor) == PIPE

        if (isAtPipe) {
            replace(
                start = cursor,
                end = cursor,
                text = SPACE
            )
        }

        selection = TextRange(index = cursor)
    }

    private companion object {
        const val ESCAPE = '\\'
        const val PIPE = '|'
        const val SPACE = " "
    }
}

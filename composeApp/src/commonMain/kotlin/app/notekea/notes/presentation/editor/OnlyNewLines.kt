package app.notekea.notes.presentation.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import kotlin.math.max
import kotlin.math.min

/**
 * The text being edited only has `\n` breaks, as [app.notekea.core.notes.domain.NoteDocument] says: Windows breaks in text
 * that reaches the field by itself (its own Paste menu, Shift+Insert) lose their `\r`, or a save would write `\r\r\n`.
 */
@OptIn(ExperimentalFoundationApi::class)
internal object OnlyNewLines : InputTransformation {
    private const val CARRIAGE_RETURN = '\r'

    override fun TextFieldBuffer.transformInput() {
        var start = length
        var end = 0
        for (change in 0 until changes.changeCount) {
            val range = changes.getRange(changeIndex = change)
            start = min(a = start, b = range.min)
            end = max(a = end, b = range.max)
        }
        for (index in minOf(end, length - 1) - 1 downTo start) {
            if (charAt(index = index) == CARRIAGE_RETURN && charAt(index = index + 1) == NEW_LINE_CHAR) {
                replace(start = index, end = index + 1, text = "")
            }
        }
    }
}

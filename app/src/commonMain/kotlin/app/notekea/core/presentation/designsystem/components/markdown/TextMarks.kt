package app.notekea.core.presentation.designsystem.components.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.mikepenz.markdown.compose.extendedspans.ExtendedSpans

/**
 * Places to mark in rendered Markdown, as Find's matches: [rangesIn] says where they are in a text as it is drawn
 * (a paragraph, a heading, a list item, without its Markdown marks), and [color] is painted behind each one.
 * [rangesIn] is asked when the text is painted: the state it reads paints the marks again, with no new layout.
 */
class TextMarks(
    val color: Color,
    val rangesIn: (String) -> List<IntRange>
) {
    /** What the renderer asks for, once for each text it draws, to paint the marks behind it. */
    internal fun spans(): @Composable () -> ExtendedSpans = {
        remember(this) { ExtendedSpans(MarksPainter(marks = this)) }
    }
}

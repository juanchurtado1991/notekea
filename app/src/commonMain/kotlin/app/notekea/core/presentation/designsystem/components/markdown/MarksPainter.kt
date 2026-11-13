package app.notekea.core.presentation.designsystem.components.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import com.mikepenz.markdown.compose.extendedspans.ExtendedSpanPainter
import com.mikepenz.markdown.compose.extendedspans.SpanDrawInstructions

/** Paints the marks behind a laid out text, and leaves its styles and links as they are. */
internal class MarksPainter(private val marks: TextMarks) : ExtendedSpanPainter() {
    override fun decorate(
        span: SpanStyle,
        start: Int,
        end: Int,
        text: AnnotatedString,
        builder: AnnotatedString.Builder
    ): SpanStyle = span

    override fun decorate(
        linkAnnotation: LinkAnnotation,
        start: Int,
        end: Int,
        text: AnnotatedString,
        builder: AnnotatedString.Builder
    ): LinkAnnotation = linkAnnotation

    override fun drawInstructionsFor(
        layoutResult: TextLayoutResult,
        color: Color?
    ): SpanDrawInstructions {
        val text = layoutResult.layoutInput.text.text

        return SpanDrawInstructions {
            marks.rangesIn(text).forEach {
                drawPath(
                    path = layoutResult.getPathForRange(
                        start = it.first,
                        end = it.last + 1
                    ),
                    color = marks.color
                )
            }
        }
    }
}

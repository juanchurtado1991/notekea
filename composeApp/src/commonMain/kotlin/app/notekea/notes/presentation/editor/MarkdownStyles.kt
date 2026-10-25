package app.notekea.notes.presentation.editor

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * How the editor shows each [HighlightKind], from the theme: headings on the type scale (28 / 20 / 16 sp, semibold),
 * code in the bundled monospaced face on a subtle background, links and wiki links in the primary color, tags in the
 * accent color, and Markdown syntax muted so the text reads first.
 */
class MarkdownStyles(private val byKind: Map<HighlightKind, SpanStyle>) {
    fun of(
        kind: HighlightKind
    ): SpanStyle = byKind.getValue(key = kind)

    companion object {
        fun from(
            colorScheme: ColorScheme,
            typography: Typography,
            codeFontFamily: FontFamily?
        ): MarkdownStyles {
            val code = SpanStyle(fontFamily = codeFontFamily, background = colorScheme.surfaceVariant)
            val headings = listOf(
                typography.headlineMedium,
                typography.titleLarge,
                typography.titleMedium,
                typography.titleMedium,
                typography.titleMedium,
                typography.titleMedium
            ).map { SpanStyle(fontSize = it.fontSize, fontWeight = FontWeight.SemiBold) }
            return MarkdownStyles(
                byKind = mapOf(
                    HighlightKind.BOLD to SpanStyle(fontWeight = FontWeight.Bold),
                    HighlightKind.CODE_BLOCK to code,
                    HighlightKind.CODE_INLINE to code,
                    HighlightKind.HEADING_1 to headings[0],
                    HighlightKind.HEADING_2 to headings[1],
                    HighlightKind.HEADING_3 to headings[2],
                    HighlightKind.HEADING_4 to headings[3],
                    HighlightKind.HEADING_5 to headings[4],
                    HighlightKind.HEADING_6 to headings[5],
                    HighlightKind.ITALIC to SpanStyle(fontStyle = FontStyle.Italic),
                    HighlightKind.LINK to SpanStyle(color = colorScheme.primary),
                    HighlightKind.SYNTAX to SpanStyle(color = colorScheme.onSurfaceVariant),
                    HighlightKind.TAG to SpanStyle(color = colorScheme.tertiary),
                    HighlightKind.WIKI_LINK to SpanStyle(color = colorScheme.primary)
                )
            )
        }
    }
}

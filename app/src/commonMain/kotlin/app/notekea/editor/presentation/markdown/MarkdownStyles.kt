package app.notekea.editor.presentation.markdown

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import app.notekea.editor.presentation.view.NO_LIGATURES

/**
 * How the editor shows each [HighlightKind], from the theme: headings on the type scale, code in the bundled monospaced
 * face on a subtle background, links and tags in the primary color, as the drawn blocks show them, and Markdown syntax
 * muted so the text reads first. Find in note marks every match, the current one more strongly.
 */
class MarkdownStyles(
    private val byKind: Map<HighlightKind, SpanStyle>,
    val findMatch: SpanStyle,
    val currentFindMatch: SpanStyle
) {
    fun of(kind: HighlightKind): SpanStyle = byKind.getValue(key = kind)

    companion object {
        private const val CURRENT_FIND_MATCH_ALPHA = 0.55f
        private const val FIND_MATCH_ALPHA = 0.22f

        /** What is painted behind each of Find's matches, in the text field and in the drawn blocks. */
        fun findMatchColor(colorScheme: ColorScheme): Color =
            colorScheme.tertiary.copy(alpha = FIND_MATCH_ALPHA)

        fun from(
            colorScheme: ColorScheme,
            typography: Typography,
            codeFontFamily: FontFamily?
        ): MarkdownStyles {
            // Code shows each typed character as it is: no ligatures (`->`, `//`, `!=`) and no contextual alternates.
            val code = SpanStyle(
                fontFamily = codeFontFamily,
                fontFeatureSettings = NO_LIGATURES,
                background = colorScheme.surfaceVariant
            )

            // The note's own scale (NotekeaTypography): it does not follow the interface's text.
            val headings = listOf(
                typography.displayMedium,
                typography.displaySmall,
                typography.bodyLarge,
                typography.bodyLarge,
                typography.bodyLarge,
                typography.bodyLarge
            ).map {
                SpanStyle(
                    fontSize = it.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
            }

            return MarkdownStyles(
                byKind = mapOf(
                    HighlightKind.BOLD to SpanStyle(fontWeight = FontWeight.Bold),
                    // A code block's background is drawn behind its whole lines, as a block, by the editor.
                    HighlightKind.CODE_BLOCK to code.copy(background = Color.Unspecified),
                    HighlightKind.CODE_INLINE to code,
                    HighlightKind.HEADING_1 to headings[0],
                    HighlightKind.HEADING_2 to headings[1],
                    HighlightKind.HEADING_3 to headings[2],
                    HighlightKind.HEADING_4 to headings[3],
                    HighlightKind.HEADING_5 to headings[4],
                    HighlightKind.HEADING_6 to headings[5],
                    // The colors the Markdown renderer gives a highlight, so it looks the same in a drawn block.
                    HighlightKind.HIGHLIGHT to SpanStyle(
                        color = colorScheme.onSecondaryContainer,
                        background = colorScheme.secondaryContainer
                    ),
                    HighlightKind.ITALIC to SpanStyle(fontStyle = FontStyle.Italic),
                    HighlightKind.LINK to SpanStyle(color = colorScheme.primary),
                    HighlightKind.MARKER to SpanStyle(color = colorScheme.onSurfaceVariant),
                    HighlightKind.STRIKETHROUGH to SpanStyle(
                        textDecoration = TextDecoration.LineThrough
                    ),
                    HighlightKind.SYNTAX to SpanStyle(color = colorScheme.onSurfaceVariant),
                    HighlightKind.TAG to SpanStyle(color = colorScheme.primary),
                    HighlightKind.WIKI_LINK to SpanStyle(color = colorScheme.primary)
                ),
                findMatch = SpanStyle(background = findMatchColor(colorScheme = colorScheme)),
                currentFindMatch = SpanStyle(
                    background = colorScheme.tertiary.copy(alpha = CURRENT_FIND_MATCH_ALPHA)
                )
            )
        }
    }
}

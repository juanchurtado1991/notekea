package app.notekea.notes.presentation.editor

import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownStylesTest {
    private val colors = lightColorScheme()
    private val typography = Typography()
    private val styles = MarkdownStyles.from(
        colorScheme = colors,
        typography = typography,
        codeFontFamily = FontFamily.Monospace
    )

    @Test
    fun `every kind has a style taken from the theme`() {
        assertEquals(
            expected = listOf(colors.primary, colors.primary, colors.tertiary, colors.onSurfaceVariant),
            actual = listOf(
                HighlightKind.LINK,
                HighlightKind.WIKI_LINK,
                HighlightKind.TAG,
                HighlightKind.SYNTAX
            ).map { styles.of(kind = it).color }
        )
        assertEquals(
            expected = FontFamily.Monospace to colors.surfaceVariant,
            actual = styles.of(kind = HighlightKind.CODE_BLOCK).let { it.fontFamily to it.background }
        )
    }

    @Test
    fun `find matches use the accent, the current one stronger`() {
        val match = styles.findMatch.background
        val current = styles.currentFindMatch.background

        assertEquals(
            expected = listOf(colors.tertiary.red, colors.tertiary.red),
            actual = listOf(match.red, current.red)
        )
        assertEquals(
            expected = true,
            actual = current.alpha > match.alpha
        )
    }

    @Test
    fun `headings follow the type scale, largest first`() {
        assertEquals(
            expected = listOf(
                typography.headlineMedium.fontSize,
                typography.titleLarge.fontSize,
                typography.titleMedium.fontSize
            ),
            actual = listOf(HighlightKind.HEADING_1, HighlightKind.HEADING_2, HighlightKind.HEADING_6).map {
                styles.of(kind = it).fontSize
            }
        )
        assertEquals(
            expected = FontWeight.SemiBold,
            actual = styles.of(kind = HighlightKind.HEADING_1).fontWeight
        )
    }
}

package app.notekea.core.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Accessibility gate for the palette: every text/background pair the components use must reach WCAG AA
 * (4.5:1 for normal text) in both themes, so a token change that hurts readability fails the build.
 */
class NotekeaColorSchemesTest {
    @Test
    fun `dark theme text pairs reach WCAG AA contrast`() {
        assertTextPairsReachAa(scheme = NotekeaColorSchemes.dark)
    }

    @Test
    fun `forTheme picks the scheme matching the requested mode`() {
        assertSame(
            expected = NotekeaColorSchemes.dark,
            actual = NotekeaColorSchemes.forTheme(darkTheme = true)
        )
        assertSame(
            expected = NotekeaColorSchemes.light,
            actual = NotekeaColorSchemes.forTheme(darkTheme = false)
        )
    }

    @Test
    fun `light theme text pairs reach WCAG AA contrast`() {
        assertTextPairsReachAa(scheme = NotekeaColorSchemes.light)
    }

    private fun assertTextPairsReachAa(
        scheme: ColorScheme
    ) {
        val pairs = mapOf(
            "onBackground/background" to (scheme.onBackground to scheme.background),
            "onSurface/surface" to (scheme.onSurface to scheme.surface),
            "onSurfaceVariant/surface" to (scheme.onSurfaceVariant to scheme.surface),
            "onSurfaceVariant/background" to (scheme.onSurfaceVariant to scheme.background),
            "onPrimary/primary" to (scheme.onPrimary to scheme.primary),
            "onPrimaryContainer/primaryContainer" to (scheme.onPrimaryContainer to scheme.primaryContainer),
            "onTertiary/tertiary" to (scheme.onTertiary to scheme.tertiary),
            "onError/error" to (scheme.onError to scheme.error),
            "error/surface" to (scheme.error to scheme.surface),
            "primary/surface" to (scheme.primary to scheme.surface)
        )
        pairs.forEach { (name, colors) ->
            val ratio = contrastRatio(
                foreground = colors.first,
                background = colors.second
            )
            assertTrue(
                actual = ratio >= WCAG_AA_NORMAL_TEXT,
                message = "$name contrast is $ratio, below $WCAG_AA_NORMAL_TEXT"
            )
        }
    }

    private fun contrastRatio(
        foreground: Color,
        background: Color
    ): Double {
        val lighter = maxOf(foreground.luminance(), background.luminance()) + LUMINANCE_OFFSET
        val darker = minOf(foreground.luminance(), background.luminance()) + LUMINANCE_OFFSET
        return (lighter / darker).toDouble()
    }

    companion object {
        private const val LUMINANCE_OFFSET = 0.05f
        private const val WCAG_AA_NORMAL_TEXT = 4.5
    }
}

package app.notekea.core.presentation.theme

import androidx.compose.material3.ColorScheme
import app.notekea.core.appearance.domain.ColorTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/** Every color theme in light and dark reads at WCAG AA (`ROADMAP.md` 2.12); two have palettes of their own. */
class ColorThemeSchemesTest {
    @Test
    fun `every text of every theme and mode reads on its background at WCAG AA`() {
        val failures = ColorTheme.entries.flatMap { theme ->
            listOf(false, true).flatMap { dark ->
                val mode = if (dark) "dark" else "light"
                pairsOf(scheme = ColorThemeSchemes.of(theme = theme, darkTheme = dark))
                    .filter { (_, text, background) -> contrastOf(text, background) < WCAG_AA }
                    .map { (name, text, background) -> "$theme $mode $name %.2f".format(contrastOf(text, background)) }
            }
        }

        assertEquals(expected = emptyList(), actual = failures)
    }

    @Test
    fun `Cobalt Tangerine is the app's first palette and Workspace Blue the default`() {
        assertSame(
            expected = NotekeaColorSchemes.light,
            actual = ColorThemeSchemes.of(ColorTheme.COBALT_TANGERINE, darkTheme = false)
        )
        assertSame(
            expected = NotekeaColorSchemes.dark,
            actual = ColorThemeSchemes.of(ColorTheme.COBALT_TANGERINE, darkTheme = true)
        )
        assertSame(
            expected = WorkspaceBlueSchemes.light,
            actual = ColorThemeSchemes.of(ColorTheme.DEFAULT, darkTheme = false)
        )
        assertSame(
            expected = WorkspaceBlueSchemes.dark,
            actual = ColorThemeSchemes.of(ColorTheme.DEFAULT, darkTheme = true)
        )
    }

    /** The texts the app draws and what they are drawn on: the page, panes, selection, buttons, tags and links. */
    private fun pairsOf(scheme: ColorScheme) = with(scheme) {
        listOf(
            Triple("ink on page", onBackground, background),
            Triple("ink on surface", onSurface, surface),
            Triple("ink on variant", onSurface, surfaceVariant),
            Triple("muted on page", onSurfaceVariant, background),
            Triple("muted on surface", onSurfaceVariant, surface),
            Triple("muted on variant", onSurfaceVariant, surfaceVariant),
            Triple("button text", onPrimary, primary),
            Triple("selected text", onPrimaryContainer, primaryContainer),
            Triple("tag text", onTertiary, tertiary),
            Triple("link on page", primary, background),
            Triple("link on surface", primary, surface)
        )
    }
}

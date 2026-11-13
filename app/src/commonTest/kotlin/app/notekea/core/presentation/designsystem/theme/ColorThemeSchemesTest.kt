package app.notekea.core.presentation.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import app.notekea.core.appearance.domain.ColorTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/** Every color theme in light and dark reads at WCAG AA. */
class ColorThemeSchemesTest {
    @Test
    fun `every text of every theme and mode reads on its background at WCAG AA`() {
        val failures = ColorTheme.entries.flatMap { theme ->
            listOf(false, true).flatMap { dark ->
                val mode = if (dark) "dark" else "light"

                pairsOf(
                    scheme = ColorThemeSchemes.of(
                        theme = theme,
                        darkTheme = dark
                    )
                )
                    .filter { (_, text, background) ->
                        contrastOf(text, background) < WCAG_AA
                    }
                    .map { (name, text, background) ->
                        "$theme $mode $name %.2f".format(contrastOf(text, background))
                    }
            }
        }

        assertEquals(
            expected = emptyList(),
            actual = failures
        )
    }

    @Test
    fun `Workspace Blue is the default, and every theme paints its sidebar and tabs apart from the page`() {
        val flat = ColorTheme.entries.flatMap { theme ->
            listOf(false, true).mapNotNull { dark ->
                val scheme = ColorThemeSchemes.of(
                    theme = theme,
                    darkTheme = dark
                )

                theme.takeIf {
                    scheme.sidebarSurface == scheme.background ||
                        scheme.chromeSurface == scheme.background
                }
            }
        }

        assertEquals(
            expected = ColorTheme.WORKSPACE_BLUE to emptyList(),
            actual = ColorTheme.DEFAULT to flat
        )
    }

    @Test
    fun `Workspace Blue keeps its own palette`() {
        assertSame(
            expected = WorkspaceBlueSchemes.light,
            actual = ColorThemeSchemes.of(ColorTheme.WORKSPACE_BLUE, darkTheme = false)
        )

        assertSame(
            expected = WorkspaceBlueSchemes.dark,
            actual = ColorThemeSchemes.of(ColorTheme.WORKSPACE_BLUE, darkTheme = true)
        )
    }

    /** The lightest a card's label sits on: a white picture under the bottom of its shade. */
    private fun shadeOverWhite(scrim: Color): Color = scrim.copy(
        alpha = CARD_SHADE_ALPHA
    ).compositeOver(background = Color.White)

    /** The texts the app draws and what they are drawn on: the page, panes, selection, buttons, tags and links. */
    private fun pairsOf(scheme: ColorScheme) = with(scheme) {
        listOf(
            Triple("ink on page", onBackground, background),
            Triple("ink on surface", onSurface, surface),
            Triple("ink on variant", onSurface, surfaceVariant),
            Triple("muted on page", onSurfaceVariant, background),
            Triple(
                "missing bookmark",
                missingEntry.compositeOver(background = background),
                background
            ),
            Triple(
                "missing selected bookmark",
                missingEntry.compositeOver(background = surfaceVariant),
                surfaceVariant
            ),
            Triple(
                "card label on a white picture",
                NotekeaColors.onImage,
                shadeOverWhite(scrim = scrim)
            ),
            Triple("muted on surface", onSurfaceVariant, surface),
            Triple("muted on variant", onSurfaceVariant, surfaceVariant),
            Triple("button text", onPrimary, primary),
            Triple("selected text", onPrimaryContainer, primaryContainer),
            Triple("tag text", onTertiary, tertiary),
            Triple("link on page", primary, background),
            Triple("link on surface", primary, surface),
            // The macOS redesign's sidebar and chrome (4A), and the source list's filled selection (2A).
            Triple("ink on sidebar", onSurface, sidebarSurface),
            Triple("muted on sidebar", onSurfaceVariant, sidebarSurface),
            Triple("link on sidebar", primary, sidebarSurface),
            Triple("selected row", onPrimary, primary),
            Triple("selected row without the keyboard", onSurface, outline),
            Triple("ink on chrome", onSurface, chromeSurface),
            Triple("muted on chrome", onSurfaceVariant, chromeSurface)
        )
    }

    private companion object {
        const val CARD_SHADE_ALPHA = 0.7f
    }
}

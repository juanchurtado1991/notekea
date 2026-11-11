package app.notekea.core.presentation.designsystem.theme

import androidx.compose.ui.graphics.Color
import app.notekea.core.appearance.domain.ColorTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** Any accent a person picks in the system keeps every text readable, in every theme and mode (redesign 4B). */
class AccentSchemesTest {
    @Test
    fun `every system accent reads at WCAG AA on every theme in both modes`() {
        val failing = ACCENTS.flatMap { accent ->
            ColorTheme.entries.flatMap { theme ->
                listOf(true, false).flatMap { dark ->
                    val scheme = ColorThemeSchemes
                        .of(
                            theme = theme,
                            darkTheme = dark
                        )
                        .withAccent(
                            accent = accent,
                            darkTheme = dark
                        )

                    with(scheme) {
                        listOf(
                            "link on page" to contrastOf(primary, background),
                            "link on note" to contrastOf(primary, surface),
                            "link on sidebar" to contrastOf(primary, sidebarSurface),
                            "button text" to contrastOf(onPrimary, primary),
                            "selected text" to contrastOf(onPrimaryContainer, primaryContainer)
                        )
                    }
                        .filter { (_, ratio) ->
                            ratio < WCAG_AA
                        }
                        .map { (name, ratio) -> "$theme ${if (dark) "dark" else "light"} $accent $name $ratio" }
                }
            }
        }

        assertEquals(
            expected = emptyList(),
            actual = failing
        )
    }

    private companion object {
        const val WCAG_AA = 4.5f

        // macOS's eight accents and Windows' default blue.
        val ACCENTS = listOf(
            Color(color = 0xFF8C8C8C),
            Color(color = 0xFFFF5257),
            Color(color = 0xFFF7821B),
            Color(color = 0xFFFFC600),
            Color(color = 0xFF62BA46),
            Color(color = 0xFF007AFF),
            Color(color = 0xFFA550A7),
            Color(color = 0xFFF74F9E),
            Color(color = 0xFF0078D7)
        )
    }
}

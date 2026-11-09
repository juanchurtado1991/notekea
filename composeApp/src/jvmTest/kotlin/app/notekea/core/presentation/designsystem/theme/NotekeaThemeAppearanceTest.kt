package app.notekea.core.presentation.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.ThemeMode
import app.notekea.core.presentation.UiThread
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `ROADMAP.md` 2.12: the app's theme follows the appearance chosen in Settings (its color theme, always light or dark,
 * or as the system is), whatever the system says when the mode is not System.
 */
class NotekeaThemeAppearanceTest {
    @Test
    fun `the theme draws the chosen color theme in the chosen mode, or the system's under System`() {
        val cases = listOf(
            Appearance(theme = ColorTheme.SAGE, mode = ThemeMode.DARK) to false,
            Appearance(theme = ColorTheme.ROSE, mode = ThemeMode.LIGHT) to true,
            Appearance(theme = ColorTheme.LAGOON, mode = ThemeMode.SYSTEM) to true,
            Appearance() to false
        )

        val drawn = cases.map { (appearance, isSystemDark) -> schemeDrawn(appearance, isSystemDark) }

        assertEquals(
            expected = listOf(
                ColorThemeSchemes.of(theme = ColorTheme.SAGE, darkTheme = true),
                ColorThemeSchemes.of(theme = ColorTheme.ROSE, darkTheme = false),
                ColorThemeSchemes.of(theme = ColorTheme.LAGOON, darkTheme = true),
                WorkspaceBlueSchemes.light
            ),
            actual = drawn
        )
    }

    private fun schemeDrawn(
        appearance: Appearance,
        isSystemDark: Boolean
    ): ColorScheme? {
        var scheme: ColorScheme? = null
        UiThread.run {
            ImageComposeScene(width = SIZE, height = SIZE, density = Density(density = 1f)) {
                CompositionLocalProvider(
                    LocalAppearance provides appearance,
                    LocalSystemDarkTheme provides isSystemDark
                ) {
                    NotekeaTheme { scheme = MaterialTheme.colorScheme }
                }
            }.use { it.render() }
        }
        return scheme
    }

    private companion object {
        const val SIZE = 10
    }
}

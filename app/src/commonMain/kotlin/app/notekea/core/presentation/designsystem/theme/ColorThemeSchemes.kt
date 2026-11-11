package app.notekea.core.presentation.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.presentation.designsystem.theme.palette.ThemeColorSets
import app.notekea.core.presentation.designsystem.theme.palette.ThemeColors
import app.notekea.core.presentation.designsystem.theme.palette.WorkspaceBluePalette

/**
 * The light and dark color schemes of every [ColorTheme], with the same roles as
 * [WorkspaceBlueSchemes], which [ColorTheme.WORKSPACE_BLUE] uses as it is; the others come from their [ThemeColors].
 */
object ColorThemeSchemes {
    private val schemes: Map<Pair<ColorTheme, Boolean>, ColorScheme> by lazy {
        ColorTheme
            .entries
            .flatMap { theme ->
                listOf(true, false).map { dark ->
                    (theme to dark) to build(
                        theme = theme,
                        darkTheme = dark
                    )
                }
            }
            .toMap()
    }

    fun of(
        theme: ColorTheme,
        darkTheme: Boolean
    ): ColorScheme = schemes.getValue(key = theme to darkTheme)

    private fun build(
        theme: ColorTheme,
        darkTheme: Boolean
    ): ColorScheme = when {
        theme == ColorTheme.WORKSPACE_BLUE -> WorkspaceBlueSchemes.forTheme(darkTheme = darkTheme)

        darkTheme -> schemeOf(
            base = darkColorScheme(),
            colors = ThemeColorSets.dark.getValue(key = theme),
            error = WorkspaceBluePalette.ErrorDark,
            onError = WorkspaceBluePalette.OnColorDark,
            sidebarInk = SIDEBAR_INK_DARK
        )

        else -> schemeOf(
            base = lightColorScheme(),
            colors = ThemeColorSets.light.getValue(key = theme),
            error = WorkspaceBluePalette.ErrorLight,
            onError = WorkspaceBluePalette.OnColorLight,
            sidebarInk = SIDEBAR_INK_LIGHT
        )
    }

    private fun schemeOf(
        base: ColorScheme,
        colors: ThemeColors,
        error: Color,
        onError: Color,
        sidebarInk: Float
    ): ColorScheme = with(colors) {
        base.copy(
            primary = lead,
            onPrimary = onLead,
            primaryContainer = selection,
            onPrimaryContainer = ink,
            tertiary = accent,
            onTertiary = WorkspaceBluePalette.OnColorDark,
            background = page,
            onBackground = ink,
            surface = surface,
            // The sidebar and the chrome (4A): the page a little toward the ink, so they keep the theme's hue; on a
            // light page only as far as links on the sidebar still reach WCAG AA.
            surfaceDim = lerp(
                start = page,
                stop = ink,
                fraction = sidebarInk
            ),
            surfaceBright = lerp(
                start = page,
                stop = ink,
                fraction = CHROME_INK
            ),
            onSurface = ink,
            surfaceVariant = raised,
            onSurfaceVariant = muted,
            outline = outline,
            outlineVariant = raised,
            error = error,
            onError = onError
        )
    }

    private const val SIDEBAR_INK_DARK = 0.05f
    private const val SIDEBAR_INK_LIGHT = 0.03f
    private const val CHROME_INK = 0.03f
}

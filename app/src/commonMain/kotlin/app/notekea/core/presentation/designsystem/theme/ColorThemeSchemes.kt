package app.notekea.core.presentation.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.presentation.designsystem.theme.palette.ThemeColorSets
import app.notekea.core.presentation.designsystem.theme.palette.ThemeColors
import app.notekea.core.presentation.designsystem.theme.palette.WorkspaceBluePalette

/**
 * The light and dark color schemes of every [ColorTheme]: [ColorTheme.WORKSPACE_BLUE] uses [WorkspaceBlueSchemes] as it
 * is, and the others are built from their [ThemeColors].
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
            onError = WorkspaceBluePalette.OnColorDark
        )

        else -> schemeOf(
            base = lightColorScheme(),
            colors = ThemeColorSets.light.getValue(key = theme),
            error = WorkspaceBluePalette.ErrorLight,
            onError = WorkspaceBluePalette.OnColorLight
        )
    }

    private fun schemeOf(
        base: ColorScheme,
        colors: ThemeColors,
        error: Color,
        onError: Color
    ): ColorScheme = with(colors) {
        base.copy(
            primary = lead,
            onPrimary = onLead,
            primaryContainer = selection,
            onPrimaryContainer = ink,
            secondary = lead,
            onSecondary = onLead,
            secondaryContainer = raised,
            onSecondaryContainer = ink,
            surfaceContainerLowest = page,
            surfaceContainerLow = surface,
            surfaceContainer = sidebar,
            surfaceContainerHigh = raised,
            surfaceContainerHighest = raised,
            scrim = Color.Black,
            tertiary = accent,
            onTertiary = WorkspaceBluePalette.OnColorDark,
            background = page,
            onBackground = ink,
            surface = surface,
            surfaceDim = sidebar,
            surfaceBright = chrome,
            onSurface = ink,
            surfaceVariant = raised,
            onSurfaceVariant = muted,
            outline = outline,
            outlineVariant = raised,
            error = error,
            onError = onError
        )
    }
}

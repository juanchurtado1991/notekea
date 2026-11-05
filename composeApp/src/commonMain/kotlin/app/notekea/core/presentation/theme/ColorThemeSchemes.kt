package app.notekea.core.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.presentation.theme.palette.ThemeColorSets
import app.notekea.core.presentation.theme.palette.ThemeColors
import app.notekea.core.presentation.theme.palette.WorkspaceBluePalette

/**
 * The light and dark color schemes of every [ColorTheme] (`ROADMAP.md` 2.12), with the same roles as
 * [WorkspaceBlueSchemes], which [ColorTheme.WORKSPACE_BLUE] uses as it is; the others come from their [ThemeColors].
 */
object ColorThemeSchemes {
    private val schemes: Map<Pair<ColorTheme, Boolean>, ColorScheme> by lazy {
        ColorTheme.entries.flatMap { theme ->
            listOf(true, false).map { dark -> (theme to dark) to build(theme = theme, darkTheme = dark) }
        }.toMap()
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
        darkTheme -> darkSchemeOf(colors = ThemeColorSets.dark.getValue(key = theme))
        else -> lightSchemeOf(colors = ThemeColorSets.light.getValue(key = theme))
    }

    private fun darkSchemeOf(
        colors: ThemeColors
    ): ColorScheme = with(colors) {
        darkColorScheme(
            primary = lead,
            onPrimary = onLead,
            primaryContainer = selection,
            onPrimaryContainer = ink,
            tertiary = accent,
            onTertiary = WorkspaceBluePalette.OnColorDark,
            background = page,
            onBackground = ink,
            surface = surface,
            onSurface = ink,
            surfaceVariant = raised,
            onSurfaceVariant = muted,
            outline = outline,
            outlineVariant = raised,
            error = WorkspaceBluePalette.ErrorDark,
            onError = WorkspaceBluePalette.OnColorDark
        )
    }

    private fun lightSchemeOf(
        colors: ThemeColors
    ): ColorScheme = with(colors) {
        lightColorScheme(
            primary = lead,
            onPrimary = onLead,
            primaryContainer = selection,
            onPrimaryContainer = ink,
            tertiary = accent,
            onTertiary = WorkspaceBluePalette.OnColorDark,
            background = page,
            onBackground = ink,
            surface = surface,
            onSurface = ink,
            surfaceVariant = raised,
            onSurfaceVariant = muted,
            outline = outline,
            outlineVariant = raised,
            error = WorkspaceBluePalette.ErrorLight,
            onError = WorkspaceBluePalette.OnColorLight
        )
    }
}

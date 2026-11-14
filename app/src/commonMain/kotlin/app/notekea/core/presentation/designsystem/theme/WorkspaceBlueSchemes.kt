package app.notekea.core.presentation.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import app.notekea.core.presentation.designsystem.theme.palette.WorkspaceBluePalette

/** The light and dark schemes of the Workspace Blue theme, with the same roles as the app's theme roles. */
object WorkspaceBlueSchemes {
    val dark: ColorScheme = darkColorScheme(
        primary = WorkspaceBluePalette.PrimaryDark,
        onPrimary = WorkspaceBluePalette.OnColorDark,
        primaryContainer = WorkspaceBluePalette.PrimaryContainerDark,
        onPrimaryContainer = WorkspaceBluePalette.InkDark,
        secondary = WorkspaceBluePalette.PrimaryDark,
        onSecondary = WorkspaceBluePalette.OnColorDark,
        secondaryContainer = WorkspaceBluePalette.SurfaceVariantDark,
        onSecondaryContainer = WorkspaceBluePalette.InkDark,
        // The containers the components ask for by name: without them Material's own purple-grey showed.
        surfaceContainerLowest = WorkspaceBluePalette.BackgroundDark,
        surfaceContainerLow = WorkspaceBluePalette.SurfaceDark,
        surfaceContainer = WorkspaceBluePalette.SidebarDark,
        surfaceContainerHigh = WorkspaceBluePalette.SurfaceVariantDark,
        surfaceContainerHighest = WorkspaceBluePalette.SurfaceVariantDark,
        scrim = Color.Black,
        tertiary = WorkspaceBluePalette.AccentDark,
        onTertiary = WorkspaceBluePalette.OnColorDark,
        background = WorkspaceBluePalette.BackgroundDark,
        onBackground = WorkspaceBluePalette.InkDark,
        surface = WorkspaceBluePalette.SurfaceDark,
        surfaceDim = WorkspaceBluePalette.SidebarDark,
        surfaceBright = WorkspaceBluePalette.ChromeDark,
        onSurface = WorkspaceBluePalette.InkDark,
        surfaceVariant = WorkspaceBluePalette.SurfaceVariantDark,
        onSurfaceVariant = WorkspaceBluePalette.MutedDark,
        outline = WorkspaceBluePalette.OutlineDark,
        outlineVariant = WorkspaceBluePalette.OutlineVariantDark,
        error = WorkspaceBluePalette.ErrorDark,
        onError = WorkspaceBluePalette.OnColorDark
    )

    val light: ColorScheme = lightColorScheme(
        primary = WorkspaceBluePalette.PrimaryLight,
        onPrimary = WorkspaceBluePalette.OnColorLight,
        primaryContainer = WorkspaceBluePalette.PrimaryContainerLight,
        onPrimaryContainer = WorkspaceBluePalette.OnPrimaryContainerLight,
        secondary = WorkspaceBluePalette.PrimaryLight,
        onSecondary = WorkspaceBluePalette.OnColorLight,
        secondaryContainer = WorkspaceBluePalette.SurfaceVariantLight,
        onSecondaryContainer = WorkspaceBluePalette.InkLight,
        // The containers the components ask for by name: without them Material's own purple-grey showed.
        surfaceContainerLowest = WorkspaceBluePalette.BackgroundLight,
        surfaceContainerLow = WorkspaceBluePalette.SurfaceLight,
        surfaceContainer = WorkspaceBluePalette.SidebarLight,
        surfaceContainerHigh = WorkspaceBluePalette.SurfaceVariantLight,
        surfaceContainerHighest = WorkspaceBluePalette.SurfaceVariantLight,
        scrim = Color.Black,
        tertiary = WorkspaceBluePalette.AccentLight,
        onTertiary = WorkspaceBluePalette.OnColorDark,
        background = WorkspaceBluePalette.BackgroundLight,
        onBackground = WorkspaceBluePalette.InkLight,
        surface = WorkspaceBluePalette.SurfaceLight,
        surfaceDim = WorkspaceBluePalette.SidebarLight,
        surfaceBright = WorkspaceBluePalette.ChromeLight,
        onSurface = WorkspaceBluePalette.InkLight,
        surfaceVariant = WorkspaceBluePalette.SurfaceVariantLight,
        onSurfaceVariant = WorkspaceBluePalette.MutedLight,
        outline = WorkspaceBluePalette.OutlineLight,
        outlineVariant = WorkspaceBluePalette.OutlineVariantLight,
        error = WorkspaceBluePalette.ErrorLight,
        onError = WorkspaceBluePalette.OnColorLight
    )

    fun forTheme(darkTheme: Boolean): ColorScheme = if (darkTheme) dark else light
}

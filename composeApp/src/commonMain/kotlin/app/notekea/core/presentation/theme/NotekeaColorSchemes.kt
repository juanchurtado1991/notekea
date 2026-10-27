package app.notekea.core.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import app.notekea.core.presentation.theme.palette.NotekeaPalette

/**
 * Semantic color roles for the light and dark themes. The roadmap's tokens map to Material 3 roles:
 * `accent` → `tertiary`, `muted` → `onSurfaceVariant`, `ink` → `onSurface` / `onBackground`, and the quiet hairlines
 * inside a pane → `outlineVariant`.
 */
object NotekeaColorSchemes {
    val dark: ColorScheme = darkColorScheme(
        primary = NotekeaPalette.PrimaryDark,
        onPrimary = NotekeaPalette.OnPrimaryDark,
        primaryContainer = NotekeaPalette.PrimaryContainerDark,
        onPrimaryContainer = NotekeaPalette.OnPrimaryContainerDark,
        tertiary = NotekeaPalette.AccentDark,
        onTertiary = NotekeaPalette.OnAccentDark,
        background = NotekeaPalette.BackgroundDark,
        onBackground = NotekeaPalette.InkDark,
        surface = NotekeaPalette.SurfaceDark,
        onSurface = NotekeaPalette.InkDark,
        surfaceVariant = NotekeaPalette.SurfaceVariantDark,
        onSurfaceVariant = NotekeaPalette.MutedDark,
        outline = NotekeaPalette.OutlineDark,
        outlineVariant = NotekeaPalette.OutlineVariantDark,
        error = NotekeaPalette.ErrorDark,
        onError = NotekeaPalette.OnErrorDark
    )

    val light: ColorScheme = lightColorScheme(
        primary = NotekeaPalette.PrimaryLight,
        onPrimary = NotekeaPalette.OnPrimaryLight,
        primaryContainer = NotekeaPalette.PrimaryContainerLight,
        onPrimaryContainer = NotekeaPalette.OnPrimaryContainerLight,
        tertiary = NotekeaPalette.AccentLight,
        onTertiary = NotekeaPalette.OnAccentLight,
        background = NotekeaPalette.BackgroundLight,
        onBackground = NotekeaPalette.InkLight,
        surface = NotekeaPalette.SurfaceLight,
        onSurface = NotekeaPalette.InkLight,
        surfaceVariant = NotekeaPalette.SurfaceVariantLight,
        onSurfaceVariant = NotekeaPalette.MutedLight,
        outline = NotekeaPalette.OutlineLight,
        outlineVariant = NotekeaPalette.OutlineVariantLight,
        error = NotekeaPalette.ErrorLight,
        onError = NotekeaPalette.OnErrorLight
    )

    fun forTheme(
        darkTheme: Boolean
    ): ColorScheme = if (darkTheme) dark else light
}

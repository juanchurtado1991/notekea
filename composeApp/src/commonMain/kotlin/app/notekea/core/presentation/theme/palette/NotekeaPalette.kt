package app.notekea.core.presentation.theme.palette

import androidx.compose.ui.graphics.Color

/**
 * Raw brand colors of the "refined kea" palette (approved 2026-10-23): sage green from the kea's feathers, a muted
 * terracotta from its underwings and a warm paper background that is easy on the eyes in long writing sessions.
 * Only the theme package reads them; everything else uses the semantic roles of `NotekeaColorSchemes`.
 * Text on the terracotta accent is dark because white on it does not reach WCAG AA contrast.
 */
internal object NotekeaPalette {
    val AccentDark = Color(0xFFE3916F)
    val AccentLight = Color(0xFFC8684A)
    val BackgroundDark = Color(0xFF151714)
    val BackgroundLight = Color(0xFFF6F3EC)
    val ErrorDark = Color(0xFFF2B8B5)
    val ErrorLight = Color(0xFFB3261E)
    val InkDark = Color(0xFFE6E7E0)
    val InkLight = Color(0xFF24261F)
    val MutedDark = Color(0xFF9C9D93)
    val MutedLight = Color(0xFF6B6A60)
    val OnAccentDark = Color(0xFF331408)
    val OnAccentLight = Color(0xFF2A120A)
    val OnErrorDark = Color(0xFF601410)
    val OnErrorLight = Color(0xFFFFFFFF)
    val OnPrimaryContainerDark = Color(0xFFE6E7E0)
    val OnPrimaryContainerLight = Color(0xFF1E2A1F)
    val OnPrimaryDark = Color(0xFF142016)
    val OnPrimaryLight = Color(0xFFFFFFFF)
    val OutlineDark = Color(0xFF363A35)
    val OutlineLight = Color(0xFFD8D4C8)
    val PrimaryContainerDark = Color(0xFF2C3A2D)
    val PrimaryContainerLight = Color(0xFFE2EBDD)
    val PrimaryDark = Color(0xFF9EBFA0)
    val PrimaryLight = Color(0xFF4A6B4F)
    val SurfaceDark = Color(0xFF1C1F1B)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceVariantDark = Color(0xFF252924)
    val SurfaceVariantLight = Color(0xFFEFECE4)
}

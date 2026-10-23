package app.notekea.core.presentation.theme.palette

import androidx.compose.ui.graphics.Color

/**
 * Raw brand colors, inspired by the kea parrot (olive feathers, orange underwings). Only the theme package reads
 * them; everything else uses the semantic roles of `NotekeaColorSchemes`. Approved in `ROADMAP.md` (design tokens).
 * Text on the orange accent is dark because white on it does not reach WCAG AA contrast.
 */
internal object NotekeaPalette {
    val AccentDark = Color(0xFFF29A6B)
    val AccentLight = Color(0xFFE2703A)
    val BackgroundDark = Color(0xFF16181A)
    val BackgroundLight = Color(0xFFFBFAF7)
    val ErrorDark = Color(0xFFF2B8B5)
    val ErrorLight = Color(0xFFB3261E)
    val InkDark = Color(0xFFE6E8E3)
    val InkLight = Color(0xFF1D1F1C)
    val MutedDark = Color(0xFF9AA096)
    val MutedLight = Color(0xFF6B7068)
    val OnAccentDark = Color(0xFF3A1A08)
    val OnAccentLight = Color(0xFF2B1407)
    val OnErrorDark = Color(0xFF601410)
    val OnErrorLight = Color(0xFFFFFFFF)
    val OnPrimaryDark = Color(0xFF1C2B10)
    val OnPrimaryLight = Color(0xFFFFFFFF)
    val OutlineDark = Color(0xFF3A3F43)
    val OutlineLight = Color(0xFFD9DBD4)
    val PrimaryContainerDark = Color(0xFF34472A)
    val PrimaryContainerLight = Color(0xFFDDE9D0)
    val PrimaryDark = Color(0xFFA9C48F)
    val PrimaryLight = Color(0xFF4F6B3A)
    val SurfaceDark = Color(0xFF1E2124)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceVariantDark = Color(0xFF262A2E)
    val SurfaceVariantLight = Color(0xFFF1F0EB)
}

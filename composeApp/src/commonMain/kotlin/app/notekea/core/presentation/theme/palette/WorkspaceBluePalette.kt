package app.notekea.core.presentation.theme.palette

import androidx.compose.ui.graphics.Color

/**
 * Raw colors of the Workspace Blue theme (chosen 2026-11-03): quiet grays, a clear blue for actions and selection and
 * an orange accent. Only the theme package reads them; everything else uses the semantic roles of `WorkspaceBlueSchemes`.
 * Text on the orange accent and on the dark-theme blue is dark because white on them does not reach WCAG AA.
 */
internal object WorkspaceBluePalette {
    val AccentDark = Color(0xFFF68909)
    val AccentLight = Color(0xFFE06C00)
    val BackgroundDark = Color(0xFF18191A)
    val BackgroundLight = Color(0xFFF8F8F8)
    val ErrorDark = Color(0xFFFD9891)
    val ErrorLight = Color(0xFFAE2E24)
    val InkDark = Color(0xFFCECFD2)
    val InkLight = Color(0xFF292A2E)
    val MutedDark = Color(0xFFA9ABAF)
    val MutedLight = Color(0xFF505258)
    val OnColorDark = Color(0xFF1F1F21)
    val OnColorLight = Color(0xFFFFFFFF)
    val OnPrimaryContainerLight = Color(0xFF1558BC)
    val OutlineDark = Color(0xFF3D3F43)
    val OutlineLight = Color(0xFFDDDEE1)
    val OutlineVariantDark = Color(0xFF2B2C2F)
    val OutlineVariantLight = Color(0xFFEBECED)
    val PrimaryContainerDark = Color(0xFF1C2B42)
    val PrimaryContainerLight = Color(0xFFE9F2FE)
    val PrimaryDark = Color(0xFF669DF1)
    val PrimaryLight = Color(0xFF1868DB)
    val SurfaceDark = Color(0xFF1F1F21)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceVariantDark = Color(0xFF2B2C2F)
    val SurfaceVariantLight = Color(0xFFF0F1F2)
}

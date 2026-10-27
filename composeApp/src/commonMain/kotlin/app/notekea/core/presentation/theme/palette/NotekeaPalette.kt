package app.notekea.core.presentation.theme.palette

import androidx.compose.ui.graphics.Color

/**
 * Raw brand colors of the "cobalt and tangerine" palette (approved 2026-10-23): a vivid cobalt blue for actions and
 * selection, and a tangerine accent, its complementary color, that keeps a nod to the kea's orange underwings.
 * Only the theme package reads them; everything else uses the semantic roles of `NotekeaColorSchemes`.
 * Text on the tangerine accent and on the dark-theme blue is dark because white on them does not reach WCAG AA.
 */
internal object NotekeaPalette {
    val AccentDark = Color(0xFFFF9A6B)
    val AccentLight = Color(0xFFFF7A45)
    val BackgroundDark = Color(0xFF0C111A)
    val BookmarkDark = Color(0xFFF5C542)
    val BookmarkLight = Color(0xFFE5A800)
    val BackgroundLight = Color(0xFFF7F9FC)
    val ErrorDark = Color(0xFFF2B8B5)
    val ErrorLight = Color(0xFFB3261E)
    val FileImageDark = Color(0xFF5CC8A8)
    val FileImageLight = Color(0xFF13805F)
    val FileMediaDark = Color(0xFFB79CF2)
    val FileMediaLight = Color(0xFF6A44C2)
    val FilePdfDark = Color(0xFFF28B82)
    val FilePdfLight = Color(0xFFC5362B)
    val InkDark = Color(0xFFE6EBF3)
    val InkLight = Color(0xFF141A24)
    val MutedDark = Color(0xFFA6B1C2)
    val MutedLight = Color(0xFF545D6C)
    val OnAccentDark = Color(0xFF3A1503)
    val OnAccentLight = Color(0xFF3A1503)
    val OnErrorDark = Color(0xFF601410)
    val OnErrorLight = Color(0xFFFFFFFF)
    val OnPrimaryContainerDark = Color(0xFFE6EBF3)
    val OnPrimaryContainerLight = Color(0xFF0A2A6B)
    val OnPrimaryDark = Color(0xFF08224D)
    val OnPrimaryLight = Color(0xFFFFFFFF)
    val OutlineDark = Color(0xFF273142)
    val OutlineLight = Color(0xFFD6DDE8)
    val OutlineVariantDark = Color(0xFF1D2531)
    val OutlineVariantLight = Color(0xFFE5EAF1)
    val PrimaryContainerDark = Color(0xFF16294D)
    val PrimaryContainerLight = Color(0xFFDCE7FD)
    val PrimaryDark = Color(0xFF7FB0FF)
    val PrimaryLight = Color(0xFF1F5BD8)
    val SurfaceDark = Color(0xFF141B26)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceVariantDark = Color(0xFF1C2533)
    val SurfaceVariantLight = Color(0xFFEEF2F8)
}

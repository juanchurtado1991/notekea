package app.notekea.core.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import app.notekea.core.presentation.theme.palette.NotekeaPalette

/** Colors with a meaning of their own that the Material roles of `NotekeaColorSchemes` have no slot for. */
object NotekeaColors {
    /** The star of a bookmarked note or entry (`ROADMAP.md` 1.13): a warm yellow, lighter on the dark theme. */
    val bookmark: Color
        @Composable get() = themed(dark = NotekeaPalette.BookmarkDark, light = NotekeaPalette.BookmarkLight)

    /** The icon of a picture in the tree, the bookmarks and the tabs: a soft green. */
    val fileImage: Color
        @Composable get() = themed(dark = NotekeaPalette.FileImageDark, light = NotekeaPalette.FileImageLight)

    /** The icon of a sound or a video: a soft violet. */
    val fileMedia: Color
        @Composable get() = themed(dark = NotekeaPalette.FileMediaDark, light = NotekeaPalette.FileMediaLight)

    /** The icon of a PDF: a soft red. */
    val filePdf: Color
        @Composable get() = themed(dark = NotekeaPalette.FilePdfDark, light = NotekeaPalette.FilePdfLight)

    /** [dark] on the dark theme, [light] on the light one. */
    @Composable
    private fun themed(
        dark: Color,
        light: Color
    ): Color = if (MaterialTheme.colorScheme.background.luminance() < DARK_LUMINANCE) dark else light

    private const val DARK_LUMINANCE = 0.5f
}

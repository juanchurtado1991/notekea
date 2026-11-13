package app.notekea.core.presentation.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import app.notekea.core.presentation.designsystem.theme.palette.FixedPalette

/** Colors with a meaning of their own that the Material roles of `ColorThemeSchemes` have no slot for. */
object NotekeaColors {
    /** The star of a bookmarked note or entry: a warm yellow, lighter on the dark theme. */
    val bookmark: Color
        @Composable get() = themed(
            dark = FixedPalette.BookmarkDark,
            light = FixedPalette.BookmarkLight
        )

    /** The icon of a picture in the tree, the bookmarks and the tabs: a soft green. */
    val fileImage: Color
        @Composable get() = themed(
            dark = FixedPalette.FileImageDark,
            light = FixedPalette.FileImageLight
        )

    /** The icon of a sound or a video: a soft violet. */
    val fileMedia: Color
        @Composable get() = themed(
            dark = FixedPalette.FileMediaDark,
            light = FixedPalette.FileMediaLight
        )

    /** The icon of a PDF: a soft red. */
    val filePdf: Color
        @Composable get() = themed(
            dark = FixedPalette.FilePdfDark,
            light = FixedPalette.FilePdfLight
        )

    /** Text and icons on a picture, over its dark shade: white whatever the theme, as the shade is. */
    val onImage: Color = FixedPalette.OnImage

    /** A switch's thumb (macOS redesign 5A): white in both modes, as macOS draws it. */
    val switchThumb: Color = FixedPalette.OnImage

    /** What went well and needs nothing done (a note that is saved): a calm green. */
    val success: Color
        @Composable get() = themed(
            dark = FixedPalette.SuccessDark,
            light = FixedPalette.SuccessLight
        )

    /** A warning that is not an error (an embed that goes around in a circle or too deep): a warm yellow. */
    val warning: Color
        @Composable get() = themed(
            dark = FixedPalette.WarningDark,
            light = FixedPalette.WarningLight
        )

    /** [dark] on the dark theme, [light] on the light one. */
    @Composable
    private fun themed(
        dark: Color,
        light: Color
    ): Color = if (MaterialTheme.colorScheme.background.luminance() < DARK_LUMINANCE) {
        dark
    } else {
        light
    }

    private const val DARK_LUMINANCE = 0.5f
}

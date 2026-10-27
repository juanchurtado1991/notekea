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
        @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < DARK_LUMINANCE) {
            NotekeaPalette.BookmarkDark
        } else {
            NotekeaPalette.BookmarkLight
        }

    private const val DARK_LUMINANCE = 0.5f
}

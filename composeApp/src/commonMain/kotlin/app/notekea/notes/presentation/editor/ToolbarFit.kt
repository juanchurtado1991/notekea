package app.notekea.notes.presentation.editor

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Which formatting groups the editor toolbar has room for beside its tabs: all of them, or without the lists and the
 * template ([showsLists]), or the text group only, without the links either ([showsLinks]). What is left out opens
 * from More, so the toolbar always stays one line.
 */
enum class ToolbarFit(
    val showsLinks: Boolean,
    val showsLists: Boolean
) {
    ALL(showsLinks = true, showsLists = true),
    WITHOUT_LISTS(showsLinks = true, showsLists = false),
    TEXT_ONLY(showsLinks = false, showsLists = false);

    companion object {
        private const val ALL_MIN_WIDTH_DP = 520
        private const val LINKS_MIN_WIDTH_DP = 420

        /** The fit of a toolbar [width] wide. */
        fun of(
            width: Dp
        ): ToolbarFit = when {
            width >= ALL_MIN_WIDTH_DP.dp -> ALL
            width >= LINKS_MIN_WIDTH_DP.dp -> WITHOUT_LISTS
            else -> TEXT_ONLY
        }
    }
}

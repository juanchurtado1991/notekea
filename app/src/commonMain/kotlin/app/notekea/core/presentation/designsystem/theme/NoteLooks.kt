package app.notekea.core.presentation.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * How the open vault's notes look wherever they are listed: each top-level folder that holds notes has one color of
 * [GraphPalette], in name order as in the graph, and the notes of [dailyFolder] are daily notes. Paths use `/`.
 */
@Immutable
data class NoteLooks(
    val folders: List<String> = emptyList(),
    val dailyFolder: String = ""
) {
    /** The color of the top-level folder [path] is or is in; null at the vault's root and in a folder with no notes. */
    fun colorOf(path: String): Color? = folders
        .indexOf(element = path.substringBefore(delimiter = SEPARATOR))
        .takeIf {
            it >= 0
        }
        ?.let { GraphPalette.colorAt(index = it) }

    /** Whether the note at [path] is in the daily notes' folder; never when they are kept at the vault's root. */
    fun isDaily(path: String): Boolean =
        dailyFolder.isNotEmpty() && path.startsWith(prefix = dailyFolder + SEPARATOR)

    /** The icon of the note at [path]: a calendar for a daily note. */
    fun iconOf(path: String): ImageVector =
        if (isDaily(path = path)) NotekeaIcons.Calendar else NotekeaIcons.Note

    companion object {
        private const val SEPARATOR = "/"

        /** The looks of a vault with the notes at [notePaths]. */
        fun of(
            notePaths: Collection<String>,
            dailyFolder: String
        ): NoteLooks = NoteLooks(
            folders = notePaths
                .filter {
                    SEPARATOR in it
                }
                .map { it.substringBefore(delimiter = SEPARATOR) }
                .distinct()
                .sorted(),
            dailyFolder = dailyFolder
        )
    }
}

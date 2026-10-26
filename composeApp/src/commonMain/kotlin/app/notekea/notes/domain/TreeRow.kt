package app.notekea.notes.domain

import kotlin.time.Instant

/** One visible row of the vault tree, in display order; [depth] is 0 for entries at the top of the vault. */
sealed interface TreeRow {
    val depth: Int
    val path: RelativePath

    data class Folder(
        override val path: RelativePath,
        override val depth: Int,
        /** The notes and other files inside, counting subfolders. */
        val fileCount: Int,
        val isExpanded: Boolean
    ) : TreeRow

    data class Note(
        override val path: RelativePath,
        override val depth: Int,
        val modifiedAt: Instant?
    ) : TreeRow

    /** A file that is not a note (an image, a PDF…), shown with its extension. */
    data class File(
        override val path: RelativePath,
        override val depth: Int,
        val modifiedAt: Instant?
    ) : TreeRow
}

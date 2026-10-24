package app.notekea.notes.domain

import kotlin.time.Instant

/** One visible row of the vault tree, in display order; [depth] is 0 for entries at the top of the vault. */
sealed interface TreeRow {
    val depth: Int
    val path: RelativePath

    data class Folder(
        override val path: RelativePath,
        override val depth: Int,
        val noteCount: Int,
        val isExpanded: Boolean
    ) : TreeRow

    data class Note(
        override val path: RelativePath,
        override val depth: Int,
        val modifiedAt: Instant?
    ) : TreeRow
}

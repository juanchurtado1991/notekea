package app.notekea.tree.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * Where an entry dragged in the tree would go when let go over a row: into that folder, or beside that note (its
 * folder), or to the vault's root over the empty part. Null where a drop would change nothing or cannot be: where it
 * already is, and a folder into itself.
 */
object TreeDrop {
    fun folderFor(
        dragged: RelativePath,
        over: TreeRow?
    ): RelativePath? {
        val folder = when (over) {
            null -> RelativePath.ROOT
            is TreeRow.Folder -> over.path
            else -> over.path.parent ?: RelativePath.ROOT
        }

        val isSame = folder == (dragged.parent ?: RelativePath.ROOT)
        val isInside = folder == dragged || (!folder.isRoot && folder.isWithin(folder = dragged))

        return folder.takeIf {
            !isSame && !isInside
        }
    }
}

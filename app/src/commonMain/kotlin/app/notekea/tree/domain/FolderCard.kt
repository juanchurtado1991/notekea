package app.notekea.tree.domain

import app.notekea.core.notes.domain.RelativePath

/** One card of a folder's grid: what is directly inside the folder, a subfolder, note or file. */
data class FolderCard(
    val path: RelativePath,
    val kind: Kind
) {
    enum class Kind {
        FOLDER,
        NOTE,
        IMAGE,
        FILE
    }
}

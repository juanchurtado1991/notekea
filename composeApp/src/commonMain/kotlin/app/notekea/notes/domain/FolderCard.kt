package app.notekea.notes.domain

/** One card of a folder's grid (`ROADMAP.md` 1.11): what is directly inside the folder, a subfolder, note or file. */
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

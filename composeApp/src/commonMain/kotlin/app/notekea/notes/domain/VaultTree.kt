package app.notekea.notes.domain

/**
 * The notes and folders of a vault, kept flat (keyed by path) so a single change from the file watcher is applied
 * without rebuilding anything. Adding an entry also adds its missing parent folders; removing a folder removes
 * everything inside it.
 */
data class VaultTree(
    val folders: Set<RelativePath>,
    val notes: Map<RelativePath, NoteEntry>
) {
    fun withFolder(
        path: RelativePath
    ): VaultTree = copy(folders = folders + ancestorsAndSelf(path = path))

    fun withNote(
        note: NoteEntry
    ): VaultTree {
        val path = note.file.path
        val parentFolders = path.parent?.let { ancestorsAndSelf(path = it) }.orEmpty()
        return copy(folders = folders + parentFolders, notes = notes + (path to note))
    }

    fun without(
        path: RelativePath
    ): VaultTree = copy(
        folders = folders.filterNot { it.isWithin(folder = path) }.toSet(),
        notes = notes.filterKeys { !it.isWithin(folder = path) }
    )

    private fun ancestorsAndSelf(
        path: RelativePath
    ): Set<RelativePath> = generateSequence(seed = path) { it.parent }.filterNot { it.isRoot }.toSet()

    companion object {
        val EMPTY = VaultTree(folders = emptySet(), notes = emptyMap())
    }
}

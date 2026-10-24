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

    /**
     * The tree after [from] was renamed or moved to [to]: a note keeps its metadata and a folder takes its whole
     * content along, so the change shows at once instead of waiting for the file watcher.
     */
    fun renamed(
        from: RelativePath,
        to: RelativePath
    ): VaultTree {
        val withMovedFolders = folders
            .filter { it.isWithin(folder = from) }
            .fold(
                initial = without(path = from)
            ) { tree, folder -> tree.withFolder(path = moved(path = folder, from = from, to = to)) }
        return notes.values
            .filter {
                it.file.path.isWithin(
                    folder = from
                )
            }.fold(initial = withMovedFolders) { tree, note ->
                tree.withNote(
                    note = note.copy(file = note.file.copy(path = moved(path = note.file.path, from = from, to = to)))
                )
            }
    }

    fun without(
        path: RelativePath
    ): VaultTree = copy(
        folders = folders.filterNot { it.isWithin(folder = path) }.toSet(),
        notes = notes.filterKeys { !it.isWithin(folder = path) }
    )

    private fun moved(
        path: RelativePath,
        from: RelativePath,
        to: RelativePath
    ): RelativePath = RelativePath(value = to.value + path.value.removePrefix(prefix = from.value))

    private fun ancestorsAndSelf(
        path: RelativePath
    ): Set<RelativePath> = generateSequence(seed = path) { it.parent }.filterNot { it.isRoot }.toSet()

    companion object {
        val EMPTY = VaultTree(folders = emptySet(), notes = emptyMap())
    }
}

package app.notekea.notes.domain

/**
 * The notes, other files ([attachments]: images, PDFs…) and folders of a vault, kept flat (keyed by path) so a single
 * change from the file watcher is applied without rebuilding anything. Adding an entry also adds its missing parent
 * folders; removing a folder removes everything inside it.
 */
data class VaultTree(
    val folders: Set<RelativePath>,
    val notes: Map<RelativePath, NoteEntry>,
    val attachments: Map<RelativePath, NoteFile> = emptyMap()
) {
    /** Whether [path] is one of the vault's notes, other files or folders. */
    operator fun contains(
        path: RelativePath
    ): Boolean = path in notes || path in attachments || path in folders

    fun withFolder(
        path: RelativePath
    ): VaultTree = copy(folders = folders + ancestorsAndSelf(path = path))

    fun withNote(
        note: NoteEntry
    ): VaultTree {
        val path = note.file.path
        val parentFolders = path.parent?.let { ancestorsAndSelf(path = it) }.orEmpty()
        val knownFolders = folders.takeIf { it.containsAll(parentFolders) }
        // A watcher event for a note already known as it is changes nothing: the same tree, no copy.
        if (knownFolders != null && notes[path] == note) return this
        return copy(folders = knownFolders ?: (folders + parentFolders), notes = notes + (path to note))
    }

    fun withAttachment(
        file: NoteFile
    ): VaultTree {
        val parentFolders = file.path.parent?.let { ancestorsAndSelf(path = it) }.orEmpty()
        val knownFolders = folders.takeIf { it.containsAll(parentFolders) }
        if (knownFolders != null && attachments[file.path] == file) return this
        return copy(
            folders = knownFolders ?: (folders + parentFolders),
            attachments = attachments + (file.path to file)
        )
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
            ) { tree, folder -> tree.withFolder(path = folder.rebased(from = from, to = to)) }
        val withMovedNotes = notes.values
            .filter {
                it.file.path.isWithin(
                    folder = from
                )
            }.fold(initial = withMovedFolders) { tree, note ->
                tree.withNote(
                    note = note.copy(file = note.file.copy(path = note.file.path.rebased(from = from, to = to)))
                )
            }
        return attachments.values
            .filter { it.path.isWithin(folder = from) }
            .fold(initial = withMovedNotes) { tree, file ->
                tree.withAttachment(file = file.copy(path = file.path.rebased(from = from, to = to)))
            }
    }

    fun without(
        path: RelativePath
    ): VaultTree = when {
        // Only a folder has entries inside it: a note or file goes alone, and an unknown path changes nothing.
        !path.isRoot && path !in folders -> if (path in notes || path in attachments) {
            copy(notes = notes - path, attachments = attachments - path)
        } else {
            this
        }

        else -> withoutFolder(folder = path)
    }

    private fun withoutFolder(
        folder: RelativePath
    ): VaultTree = copy(
        folders = folders.filterNot { it.isWithin(folder = folder) }.toSet(),
        notes = notes.filterKeys { !it.isWithin(folder = folder) },
        attachments = attachments.filterKeys { !it.isWithin(folder = folder) }
    )

    private fun ancestorsAndSelf(
        path: RelativePath
    ): Set<RelativePath> = generateSequence(seed = path) { it.parent }.filterNot { it.isRoot }.toSet()

    companion object {
        val EMPTY = VaultTree(folders = emptySet(), notes = emptyMap())

        /** The tree that holds [notes] and the folders above them, built in one pass (no empty folders). */
        fun fromNotes(
            notes: List<NoteEntry>
        ): VaultTree {
            val folders = notes.flatMap { note ->
                generateSequence(seed = note.file.path.parent) { it.parent }.takeWhile { !it.isRoot }.toList()
            }
            return VaultTree(folders = folders.toSet(), notes = notes.associateBy { it.file.path })
        }
    }
}

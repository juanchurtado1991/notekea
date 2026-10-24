package app.notekea.notes.domain

/**
 * Turns the vault tree into the rows the sidebar shows: inside each folder, subfolders first by name, then notes by
 * name or most recently modified first (notes without a date last). Only the contents of expanded folders are listed.
 * Each folder shows how many notes it holds, counting its subfolders.
 */
class FlattenTreeUseCase(private val naturalNameComparator: NaturalNameComparator) {
    operator fun invoke(
        tree: VaultTree,
        expandedFolders: Set<RelativePath>,
        sort: TreeSort
    ): List<TreeRow> {
        val index = TreeIndex(
            foldersByParent = tree.folders.groupBy { it.parent ?: RelativePath.ROOT },
            notesByParent = tree.notes.values.groupBy { it.file.path.parent ?: RelativePath.ROOT },
            noteCounts = noteCountsOf(tree = tree)
        )
        return buildList {
            addChildren(
                index = index,
                folder = RelativePath.ROOT,
                depth = 0,
                expandedFolders = expandedFolders,
                sort = sort
            )
        }
    }

    private fun MutableList<TreeRow>.addChildren(
        index: TreeIndex,
        folder: RelativePath,
        depth: Int,
        expandedFolders: Set<RelativePath>,
        sort: TreeSort
    ) {
        subfoldersByName(index = index, folder = folder).forEach { child ->
            val isExpanded = child in expandedFolders
            add(
                TreeRow.Folder(
                    path = child,
                    depth = depth,
                    noteCount = index.noteCounts[child] ?: 0,
                    isExpanded = isExpanded
                )
            )
            if (isExpanded) {
                addChildren(
                    index = index,
                    folder = child,
                    depth = depth + 1,
                    expandedFolders = expandedFolders,
                    sort = sort
                )
            }
        }
        index.notesByParent[folder].orEmpty().sortedWith(noteOrder(sort = sort)).forEach { note ->
            add(TreeRow.Note(path = note.file.path, depth = depth, modifiedAt = note.file.modifiedAt))
        }
    }

    private fun noteCountsOf(
        tree: VaultTree
    ): Map<RelativePath, Int> {
        val counts = mutableMapOf<RelativePath, Int>()
        tree.notes.keys.forEach { path ->
            generateSequence(seed = path.parent) { it.parent }
                .takeWhile { !it.isRoot }
                .forEach { counts[it] = (counts[it] ?: 0) + 1 }
        }
        return counts
    }

    private fun noteOrder(
        sort: TreeSort
    ): Comparator<NoteEntry> {
        val byName = compareBy(naturalNameComparator) { note: NoteEntry -> note.file.path.nameWithoutExtension }
        return when (sort) {
            TreeSort.NAME -> byName

            TreeSort.MODIFIED -> compareByDescending<NoteEntry, Long?>(
                nullsFirst()
            ) { it.file.modifiedAt?.toEpochMilliseconds() }
                .then(byName)
        }
    }

    private fun subfoldersByName(
        index: TreeIndex,
        folder: RelativePath
    ): List<RelativePath> = index.foldersByParent[folder].orEmpty().sortedWith(
        compareBy(naturalNameComparator) { it.name }
    )

    /** The tree grouped once per flattening, so building the rows never scans it again. */
    private class TreeIndex(
        val foldersByParent: Map<RelativePath, List<RelativePath>>,
        val notesByParent: Map<RelativePath, List<NoteEntry>>,
        val noteCounts: Map<RelativePath, Int>
    )
}

package app.notekea.notes.domain

import kotlin.time.Instant

/**
 * Turns the vault tree into the rows the sidebar shows: inside each folder, subfolders first by name, then notes and
 * other files together (as Obsidian lists them) by name or most recently modified first (entries without a date
 * last); a note is named without `.md`, another file with its extension. Only the contents of expanded folders are
 * listed. Each folder shows how many files it holds, notes and others, counting its subfolders.
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
            attachmentsByParent = tree.attachments.values.groupBy { it.path.parent ?: RelativePath.ROOT },
            fileCounts = fileCountsOf(tree = tree)
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
                    fileCount = index.fileCounts[child] ?: 0,
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
        val notes = index.notesByParent[folder].orEmpty().map { note ->
            TreeRow.Note(path = note.file.path, depth = depth, modifiedAt = note.file.modifiedAt)
        }
        val files = index.attachmentsByParent[folder].orEmpty().map { file ->
            TreeRow.File(path = file.path, depth = depth, modifiedAt = file.modifiedAt)
        }
        addAll(elements = (notes + files).sortedWith(entryOrder(sort = sort)))
    }

    private fun fileCountsOf(
        tree: VaultTree
    ): Map<RelativePath, Int> {
        val counts = mutableMapOf<RelativePath, Int>()
        val count = { path: RelativePath ->
            generateSequence(seed = path.parent) { it.parent }
                .takeWhile { !it.isRoot }
                .forEach { counts[it] = (counts[it] ?: 0) + 1 }
        }
        // Each key set on its own: joining them would copy every path of the vault into a new set first.
        tree.notes.keys.forEach(count)
        tree.attachments.keys.forEach(count)
        return counts
    }

    private fun entryOrder(
        sort: TreeSort
    ): Comparator<TreeRow> {
        val byName = compareBy(naturalNameComparator) { row: TreeRow -> displayName(row = row) }
        return when (sort) {
            TreeSort.NAME -> byName

            TreeSort.MODIFIED -> compareByDescending<TreeRow, Long?>(
                nullsFirst()
            ) { modifiedAt(row = it)?.toEpochMilliseconds() }
                .then(byName)
        }
    }

    private fun displayName(
        row: TreeRow
    ): String = if (row is TreeRow.Note) row.path.nameWithoutExtension else row.path.name

    private fun modifiedAt(
        row: TreeRow
    ): Instant? = when (row) {
        is TreeRow.Note -> row.modifiedAt
        is TreeRow.File -> row.modifiedAt
        is TreeRow.Folder -> null
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
        val attachmentsByParent: Map<RelativePath, List<NoteFile>>,
        val fileCounts: Map<RelativePath, Int>
    )
}

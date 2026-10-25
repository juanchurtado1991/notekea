package app.notekea.notes.presentation.tree

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.FollowedLink
import app.notekea.notes.domain.LinkUpdateResult
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TreeRow
import app.notekea.notes.domain.TreePreferences
import app.notekea.notes.domain.TreeSort
import app.notekea.notes.domain.TrashLocation
import app.notekea.notes.domain.VaultTree

// Pure state transitions of the vault sidebar, unit tested directly (`ARCHITECTURE.md` §4). Rows are not computed
// here: the ViewModel flattens the tree again after any change to the tree, the expanded folders or the sort.

/** The folder new entries go to: the selected folder, the folder of the selected note, or the top of the vault. */
internal fun targetFolder(
    state: VaultTreeState
): RelativePath {
    val selected = state.selectedPath ?: return RelativePath.ROOT
    return if (selected in state.tree.folders) selected else selected.parent ?: RelativePath.ROOT
}

internal fun preferencesOf(
    state: VaultTreeState
): TreePreferences = TreePreferences(
    expandedFolders = state.expandedFolders,
    sort = state.sort,
    sidebarWidthDp = state.sidebarWidthDp,
    isSidebarCollapsed = state.isSidebarCollapsed
)

internal fun restorePreferences(
    state: VaultTreeState,
    preferences: TreePreferences
): VaultTreeState = state.copy(
    expandedFolders = preferences.expandedFolders,
    sort = preferences.sort,
    sidebarWidthDp = preferences.sidebarWidthDp,
    isSidebarCollapsed = preferences.isSidebarCollapsed
)

internal fun reduceTreeLoaded(
    state: VaultTreeState,
    tree: VaultTree
): VaultTreeState {
    // An entry being renamed is kept even when missing: a tree scanned just before it was created may arrive first.
    // A selected note is kept too: it may be open in the editor, which shows that it is gone and offers Restore or
    // Close, so its unsaved text is never dropped behind the user's back.
    val selected = state.selectedPath?.takeIf {
        it.isMarkdown || it in tree.folders || it in tree.notes || it == state.renaming?.path
    }
    return state.copy(isLoading = false, tree = tree, selectedPath = selected, loadError = null)
}

internal fun reduceLoadFailed(
    state: VaultTreeState,
    error: NotesError
): VaultTreeState = state.copy(isLoading = false, loadError = error)

internal fun reduceToggleFolder(
    state: VaultTreeState,
    folder: RelativePath
): VaultTreeState {
    val isExpanded = folder in state.expandedFolders
    val expanded = if (isExpanded) state.expandedFolders - folder else state.expandedFolders + folder
    // A selection hidden inside the folder being collapsed moves up to the folder, so it stays visible.
    val isSelectionHidden =
        isExpanded && state.selectedPath?.let { it != folder && it.isWithin(folder = folder) } == true
    return state.copy(expandedFolders = expanded, selectedPath = if (isSelectionHidden) folder else state.selectedPath)
}

internal fun reduceRowClick(
    state: VaultTreeState,
    path: RelativePath
): VaultTreeState {
    val selected = state.copy(selectedPath = path)
    return if (path in state.tree.folders) reduceToggleFolder(state = selected, folder = path) else selected
}

internal fun reduceSelectionMove(
    state: VaultTreeState,
    step: Int
): VaultTreeState {
    if (state.rows.isEmpty()) return state
    val current = state.rows.indexOfFirst { it.path == state.selectedPath }
    val next = if (current < 0) 0 else (current + step).coerceIn(minimumValue = 0, maximumValue = state.rows.lastIndex)
    return state.copy(selectedPath = state.rows[next].path)
}

internal fun reduceExpandSelected(
    state: VaultTreeState
): VaultTreeState {
    val folder = state.rows.firstOrNull { it.path == state.selectedPath } as? TreeRow.Folder ?: return state
    if (!folder.isExpanded) return state.copy(expandedFolders = state.expandedFolders + folder.path)
    val firstChild = state.rows.getOrNull(state.rows.indexOf(folder) + 1)?.takeIf { it.depth > folder.depth }
    return firstChild?.let { state.copy(selectedPath = it.path) } ?: state
}

internal fun reduceCollapseSelected(
    state: VaultTreeState
): VaultTreeState {
    val row = state.rows.firstOrNull { it.path == state.selectedPath } ?: return state
    if (row is TreeRow.Folder && row.isExpanded) return state.copy(expandedFolders = state.expandedFolders - row.path)
    val parent = row.path.parent?.takeUnless { it.isRoot } ?: return state
    return state.copy(selectedPath = parent)
}

/** Shows [path] selected, with every folder above it expanded so it is visible. */
internal fun reduceReveal(
    state: VaultTreeState,
    path: RelativePath
): VaultTreeState {
    val ancestors = generateSequence(seed = path.parent) { it.parent }.filterNot { it.isRoot }.toSet()
    return state.copy(selectedPath = path, expandedFolders = state.expandedFolders + ancestors)
}

internal fun reduceRenameStart(
    state: VaultTreeState,
    path: RelativePath
): VaultTreeState = state.copy(
    selectedPath = path,
    renaming = RenameDraft(path = path, text = path.nameWithoutExtension),
    actionError = null
)

internal fun reduceRenameOutcome(
    state: VaultTreeState,
    from: RelativePath,
    outcome: Outcome<RelativePath, NotesError>
): VaultTreeState = when (outcome) {
    is Outcome.Failure -> {
        state.copy(renaming = state.renaming?.copy(error = outcome.error))
    }

    is Outcome.Success -> {
        val to = outcome.value
        state.copy(
            tree = state.tree.renamed(from = from, to = to),
            expandedFolders = state.expandedFolders.map { it.rebased(from = from, to = to) }.toSet(),
            selectedPath = to,
            renaming = null
        )
    }
}

internal fun reduceCreated(
    state: VaultTreeState,
    outcome: Outcome<RelativePath, NotesError>,
    isFolder: Boolean
): VaultTreeState = when (outcome) {
    is Outcome.Failure -> {
        state.copy(actionError = outcome.error)
    }

    is Outcome.Success -> {
        val path = outcome.value
        val tree = if (isFolder) {
            state.tree.withFolder(
                path = path
            )
        } else {
            state.tree.withNote(note = placeholderNote(path = path))
        }
        reduceRenameStart(state = reduceReveal(state = state.copy(tree = tree), path = path), path = path)
    }
}

internal fun reduceFollowedLink(
    state: VaultTreeState,
    outcome: Outcome<FollowedLink, NotesError>
): VaultTreeState = when (outcome) {
    is Outcome.Failure -> state.copy(actionError = outcome.error)

    is Outcome.Success -> {
        val followed = outcome.value
        val tree = if (followed.isNew) state.tree.withNote(note = placeholderNote(path = followed.path)) else state.tree
        reduceReveal(state = state.copy(tree = tree), path = followed.path)
    }
}

internal fun reduceDeleted(
    state: VaultTreeState,
    path: RelativePath,
    outcome: Outcome<TrashLocation, NotesError>
): VaultTreeState = when (outcome) {
    is Outcome.Failure -> {
        state.copy(pendingDelete = null, actionError = outcome.error)
    }

    is Outcome.Success -> {
        val notice = TreeNotice.MovedToVaultTrash(name = path.nameWithoutExtension).takeIf {
            outcome.value ==
                TrashLocation.VAULT_TRASH_FOLDER
        }
        state.copy(
            tree = state.tree.without(path = path),
            expandedFolders = state.expandedFolders.filterNot { it.isWithin(folder = path) }.toSet(),
            selectedPath = neighborOf(rows = state.rows, path = path),
            pendingDelete = null,
            notice = notice
        )
    }
}

internal fun reduceLinksUpdated(
    state: VaultTreeState,
    result: LinkUpdateResult
): VaultTreeState = state.copy(
    notice = TreeNotice.LinksUpdated(
        notes = result.updated.map { it.nameWithoutExtension }.sorted(),
        skippedCount = result.skipped.size
    )
)

/** The row a selection moves to when [path] goes away: the next one outside it, else the one before it. */
private fun neighborOf(
    rows: List<TreeRow>,
    path: RelativePath
): RelativePath? {
    val index = rows.indexOfFirst { it.path == path }
    if (index < 0) return null
    val after = rows.drop(n = index + 1).firstOrNull { !it.path.isWithin(folder = path) }
    return (after ?: rows.getOrNull(index = index - 1))?.path
}

/** A note just created, shown at once; the watcher replaces it with the real file within moments. */
private fun placeholderNote(
    path: RelativePath
): NoteEntry = NoteEntry(file = NoteFile(path = path, size = null, modifiedAt = null), metadata = NoteMetadata.NONE)

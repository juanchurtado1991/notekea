package app.notekea.notes.presentation.switcher

import app.notekea.notes.domain.ContentSearchResult
import app.notekea.notes.domain.NoteMatchField
import app.notekea.notes.domain.NoteSearchResult
import app.notekea.notes.domain.RelativePath

// Pure state transitions of the quick switcher, unit tested directly (`ARCHITECTURE.md` §4).

internal fun reduceOpened(
    state: QuickSwitcherState,
    mode: QuickSwitcherMode,
    query: String,
    purpose: QuickSwitcherPurpose
): QuickSwitcherState = state.copy(isOpen = true, mode = mode, query = query, selectedIndex = 0, purpose = purpose)

/** Moves the selection by [step], wrapping around at both ends as `Enter` / `Shift+Enter` do in Find in note. */
internal fun reduceSelectionMove(
    state: QuickSwitcherState,
    step: Int
): QuickSwitcherState {
    if (state.rows.isEmpty()) return state
    return state.copy(selectedIndex = (state.selectedIndex + step).mod(other = state.rows.size))
}

internal fun noteRowOf(
    result: NoteSearchResult
): QuickSwitcherRow {
    val path = result.note.file.path
    val isName = result.field == NoteMatchField.NAME
    val hasDetail = result.field == NoteMatchField.ALIAS || result.field == NoteMatchField.PATH
    return QuickSwitcherRow(
        path = path,
        name = path.nameWithoutExtension,
        nameRanges = if (isName) result.ranges else emptyList(),
        detailRanges = if (hasDetail) result.ranges else emptyList(),
        folder = folderOf(path = path),
        detail = result.text.takeIf { hasDetail }
    )
}

internal fun contentRowOf(
    result: ContentSearchResult
): QuickSwitcherRow {
    val path = result.note.file.path
    return QuickSwitcherRow(
        path = path,
        name = path.nameWithoutExtension,
        detailRanges = result.ranges,
        folder = folderOf(path = path),
        detail = result.snippet
    )
}

private fun folderOf(
    path: RelativePath
): String? = path.parent?.takeUnless { it.isRoot }?.value

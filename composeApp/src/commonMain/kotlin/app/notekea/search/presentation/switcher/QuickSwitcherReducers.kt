package app.notekea.search.presentation.switcher

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.IndexRunError
import app.notekea.search.domain.ContentSearchResult
import app.notekea.search.domain.NoteMatchField
import app.notekea.search.domain.NoteSearchResult
import app.notekea.search.domain.PassageResult
import app.notekea.search.domain.SearchNoteContentUseCase
import app.notekea.core.notes.domain.RelativePath

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

internal fun passageRowOf(
    result: PassageResult
): QuickSwitcherRow {
    val path = result.note.file.path
    return QuickSwitcherRow(
        path = path,
        name = path.nameWithoutExtension,
        detailRanges = result.ranges,
        folder = listOfNotNull(folderOf(path = path), result.heading)
            .joinToString(separator = PLACE_SEPARATOR)
            .ifEmpty { null },
        detail = result.snippet,
        passage = result.range
    )
}

/**
 * The hybrid search answered: its passages come first, then the notes only the exact words found, and the row that was
 * selected stays selected. A search that could not run leaves the exact words, and says so.
 */
internal fun reduceHybridAnswer(
    state: QuickSwitcherState,
    exact: List<QuickSwitcherRow>,
    outcome: Outcome<List<PassageResult>, IndexRunError>
): QuickSwitcherState {
    val passages = (outcome as? Outcome.Success)?.value.orEmpty().map(transform = ::passageRowOf)
    val found = passages.mapTo(destination = HashSet()) { it.path }
    val rows = (passages + exact.filter { it.path !in found }).take(n = SearchNoteContentUseCase.DEFAULT_LIMIT)
    val selected = state.rows.getOrNull(index = state.selectedIndex)?.path
    return state.copy(
        rows = rows,
        selectedIndex = rows.indexOfFirst { it.path == selected }.coerceAtLeast(minimumValue = 0),
        isSearching = false,
        isMeaningOff = outcome is Outcome.Failure
    )
}

private const val PLACE_SEPARATOR = " › "

private fun folderOf(
    path: RelativePath
): String? = path.parent?.takeUnless { it.isRoot }?.value

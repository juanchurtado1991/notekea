package app.notekea.notes.presentation.tree

import app.notekea.core.domain.Outcome
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.notes.domain.FlattenTreeUseCase
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.entry
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TrashLocation
import app.notekea.notes.domain.TreeSort
import app.notekea.notes.domain.VaultTree
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VaultTreeReducersTest {
    private val flattenTree = FlattenTreeUseCase(naturalNameComparator = NaturalNameComparator())
    private val tree = listOf(WELCOME, TOMATO_PLAN, ROADMAP, LISBON).fold(initial = VaultTree.EMPTY) { tree, path ->
        tree.withNote(note = entry(path = path))
    }

    @Test
    fun `new entries go to the selected folder, the selected note's folder or the top of the vault`() {
        assertEquals(
            expected = listOf(path(GARDEN), path(PROJECTS), RelativePath.ROOT),
            actual = listOf(path(GARDEN), path(ROADMAP), null).map { targetFolder(state = state(selected = it)) }
        )
    }

    @Test
    fun `a loaded tree ends loading, drops a selection that no longer exists and keeps one being renamed`() {
        val renaming = state(
            selected = path(NEW_NOTE)
        ).copy(renaming = RenameDraft(path = path(NEW_NOTE), text = "New"))

        assertEquals(
            expected = listOf(null, path(NEW_NOTE)),
            actual = listOf(
                reduceTreeLoaded(state = state(selected = path("Gone.md")), tree = tree).selectedPath,
                reduceTreeLoaded(state = renaming, tree = tree).selectedPath
            )
        )
        assertEquals(
            expected = false,
            actual = reduceTreeLoaded(state = state().copy(isLoading = true), tree = tree).isLoading
        )
    }

    @Test
    fun `clicking a folder selects and toggles it, clicking a note only selects it`() {
        val clickedFolder = reduceRowClick(state = state(), path = path(PROJECTS))
        val clickedNote = reduceRowClick(state = state(), path = path(WELCOME))

        assertEquals(
            expected = listOf(setOf(path(PROJECTS)), emptySet()),
            actual = listOf(clickedFolder.expandedFolders, clickedNote.expandedFolders)
        )
        assertEquals(
            expected = path(WELCOME),
            actual = clickedNote.selectedPath
        )
    }

    @Test
    fun `collapsing a folder moves a hidden selection up to it`() {
        val collapsed = reduceToggleFolder(
            state = state(expanded = setOf(PROJECTS, GARDEN), selected = path(TOMATO_PLAN)),
            folder = path(PROJECTS)
        )

        assertEquals(
            expected = path(PROJECTS) to setOf(path(GARDEN)),
            actual = collapsed.selectedPath to collapsed.expandedFolders
        )
    }

    @Test
    fun `arrow keys move through the visible rows and stop at the ends`() {
        val start = state(selected = null)
        val first = reduceSelectionMove(state = start, step = 1)
        val beforeFirst = reduceSelectionMove(state = first, step = -1)
        val last = reduceSelectionMove(state = state(selected = path(WELCOME)), step = 1)

        assertEquals(
            expected = listOf(path(PROJECTS), path(PROJECTS), path(WELCOME)),
            actual = listOf(first.selectedPath, beforeFirst.selectedPath, last.selectedPath)
        )
    }

    @Test
    fun `right expands a folder, then enters it, and does nothing on a note`() {
        val expanded = reduceExpandSelected(state = state(selected = path(PROJECTS)))
        val entered = reduceExpandSelected(state = state(expanded = setOf(PROJECTS), selected = path(PROJECTS)))
        val onNote = reduceExpandSelected(state = state(selected = path(WELCOME)))

        assertEquals(
            expected = listOf(setOf(path(PROJECTS)), path(GARDEN), path(WELCOME)),
            actual = listOf(expanded.expandedFolders, entered.selectedPath, onNote.selectedPath)
        )
    }

    @Test
    fun `left collapses an open folder, otherwise goes up to the parent folder`() {
        val collapsed = reduceCollapseSelected(state = state(expanded = setOf(PROJECTS), selected = path(PROJECTS)))
        val parent = reduceCollapseSelected(state = state(expanded = setOf(PROJECTS), selected = path(ROADMAP)))
        val topLevel = reduceCollapseSelected(state = state(selected = path(WELCOME)))

        assertEquals(
            expected = listOf(emptySet<RelativePath>(), setOf(path(PROJECTS)), emptySet()),
            actual = listOf(collapsed.expandedFolders, parent.expandedFolders, topLevel.expandedFolders)
        )
        assertEquals(
            expected = listOf(path(PROJECTS), path(PROJECTS), path(WELCOME)),
            actual = listOf(collapsed.selectedPath, parent.selectedPath, topLevel.selectedPath)
        )
    }

    @Test
    fun `a created note appears at once, revealed and being renamed`() {
        val created = reduceCreated(
            state = state(),
            outcome = Outcome.Success(value = path("$GARDEN/Untitled.md")),
            isFolder = false
        )

        assertEquals(
            expected = RenameDraft(path = path("$GARDEN/Untitled.md"), text = "Untitled"),
            actual = created.renaming
        )
        assertEquals(
            expected = setOf(path(PROJECTS), path(GARDEN)),
            actual = created.expandedFolders
        )
        assertEquals(
            expected = true,
            actual = path("$GARDEN/Untitled.md") in created.tree.notes
        )
    }

    @Test
    fun `a failed creation shows an action error`() {
        val failed = reduceCreated(
            state = state(),
            outcome = Outcome.Failure(error = NotesError.OperationFailed(path = GARDEN)),
            isFolder = true
        )

        assertEquals(
            expected = NotesError.OperationFailed(path = GARDEN),
            actual = failed.actionError
        )
    }

    @Test
    fun `a refused rename keeps the draft with its error, a done one moves selection and open folders`() {
        val renaming = reduceRenameStart(state = state(expanded = setOf(PROJECTS, GARDEN)), path = path(PROJECTS))
        val refused = reduceRenameOutcome(
            state = renaming,
            from = path(PROJECTS),
            outcome = Outcome.Failure(error = NotesError.InvalidName)
        )
        val done = reduceRenameOutcome(
            state = renaming,
            from = path(PROJECTS),
            outcome = Outcome.Success(value = path("Work"))
        )

        assertEquals(
            expected = RenameDraft(path = path(PROJECTS), text = PROJECTS, error = NotesError.InvalidName),
            actual = refused.renaming
        )
        assertEquals(
            expected = listOf(null, path("Work"), setOf(path("Work"), path("Work/Garden"))),
            actual = listOf(done.renaming, done.selectedPath, done.expandedFolders)
        )
    }

    @Test
    fun `a deleted entry leaves the tree and the selection moves to its neighbor`() {
        val asking = state(expanded = setOf(TRAVEL), selected = path(LISBON)).copy(pendingDelete = path(LISBON))

        val deleted = reduceDeleted(
            state = asking,
            path = path(LISBON),
            outcome = Outcome.Success(value = TrashLocation.SYSTEM_TRASH)
        )

        assertEquals(
            expected = listOf(path(WELCOME), null, null),
            actual = listOf(deleted.selectedPath, deleted.pendingDelete, deleted.notice)
        )
        assertNull(actual = deleted.tree.notes[path(LISBON)])
    }

    @Test
    fun `a delete into the vault trash folder says so, and a failed one shows an error`() {
        val asking = state(selected = path(WELCOME)).copy(pendingDelete = path(WELCOME))

        assertEquals(
            expected = TreeNotice.MovedToVaultTrash(name = "Welcome"),
            actual = reduceDeleted(
                state = asking,
                path = path(WELCOME),
                outcome = Outcome.Success(value = TrashLocation.VAULT_TRASH_FOLDER)
            ).notice
        )
        assertEquals(
            expected = NotesError.EntryMissing(path = WELCOME) to null,
            actual = reduceDeleted(
                state = asking,
                path = path(WELCOME),
                outcome = Outcome.Failure(error = NotesError.EntryMissing(path = WELCOME))
            ).let { it.actionError to it.pendingDelete }
        )
    }

    private fun state(
        expanded: Set<String> = emptySet(),
        selected: RelativePath? = null
    ): VaultTreeState {
        val expandedFolders = expanded.map { path(it) }.toSet()
        return VaultTreeState(
            vaultPath = VAULT,
            platformConventions = PreviewPlatformConventions.linux,
            isLoading = false,
            tree = tree,
            rows = flattenTree(tree = tree, expandedFolders = expandedFolders, sort = TreeSort.NAME),
            expandedFolders = expandedFolders,
            selectedPath = selected
        )
    }

    private fun path(
        value: String
    ): RelativePath = RelativePath(value = value)

    companion object {
        private const val GARDEN = "Projects/Garden"
        private const val LISBON = "Travel/Lisbon trip.md"
        private const val NEW_NOTE = "New note.md"
        private const val PROJECTS = "Projects"
        private const val ROADMAP = "Projects/Roadmap.md"
        private const val TOMATO_PLAN = "Projects/Garden/Tomato plan.md"
        private const val TRAVEL = "Travel"
        private const val WELCOME = "Welcome.md"
    }
}

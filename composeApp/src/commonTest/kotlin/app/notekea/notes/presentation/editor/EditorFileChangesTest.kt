package app.notekea.notes.presentation.editor

import androidx.compose.runtime.snapshots.Snapshot
import app.notekea.notes.domain.LinkUpdateResult
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

/** The open note's file changing under the editor */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorFileChangesTest : EditorViewModelTestBase() {
    @Test
    fun `an outside change reloads a note without unsaved edits`() = runEditorTest {
        open(path = AREPAS)

        changeOutside(text = "---\ntags: [recipe]\n---\n# Arepas from the phone\n")

        assertEquals(
            expected = false to "# Arepas from the phone\n",
            actual = viewModel.state.value.hasConflict to viewModel.buffer.text.toString()
        )
    }

    @Test
    fun `a read of the editor's own earlier save, reported after a later one, is no outside change`() = runEditorTest {
        open(path = AREPAS)
        type(text = "First")
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()
        val first = store.texts.getValue(AREPAS)
        type(text = "First and second")
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()

        // The watcher read the file between the two saves and reports it only now (1.14-02 on macOS CI).
        store.staleReads += first
        scanner.addNote(file = file(path = AREPAS.value, modifiedOffsetMillis = 2L))
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
        Snapshot.sendApplyNotifications()
        runCurrent()

        assertEquals(
            expected = Triple(false, "First and second", "---\ntags: [recipe]\n---\nFirst and second"),
            actual = Triple(
                viewModel.state.value.hasConflict,
                viewModel.buffer.text.toString(),
                store.texts.getValue(AREPAS)
            )
        )
    }

    @Test
    fun `an outside change with unsaved edits asks, and autosave waits for the answer`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Mine")

        changeOutside(text = "Theirs")
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
        runCurrent()

        assertEquals(
            expected = Triple(true, "Theirs", "Mine"),
            actual = Triple(
                viewModel.state.value.hasConflict,
                store.texts.getValue(AREPAS),
                viewModel.buffer.text.toString()
            )
        )
    }

    @Test
    fun `keep mine overwrites the outside change with the editor's whole note`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Mine")
        changeOutside(text = "Theirs")

        viewModel.onIntent(intent = EditorIntent.OnKeepMine)
        runCurrent()

        assertEquals(
            expected = "---\ntags: [recipe]\n---\nMine" to false,
            actual = store.texts.getValue(AREPAS) to viewModel.state.value.hasConflict
        )
    }

    @Test
    fun `load theirs drops the unsaved edits`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Mine")
        changeOutside(text = "Theirs")

        viewModel.onIntent(intent = EditorIntent.OnLoadTheirs)
        runCurrent()

        assertEquals(
            expected = Triple("Theirs", false, SaveStatus.NONE),
            actual = Triple(
                viewModel.buffer.text.toString(),
                viewModel.state.value.hasConflict,
                viewModel.state.value.saveStatus
            )
        )
    }

    @Test
    fun `a note deleted while open can be restored with the editor's text`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Still here")

        deleteOutside()
        val deleted = viewModel.state.value.isDeleted
        viewModel.onIntent(intent = EditorIntent.OnRestore)
        runCurrent()

        assertEquals(
            expected = Triple(true, false, "---\ntags: [recipe]\n---\nStill here"),
            actual = Triple(deleted, viewModel.state.value.isDeleted, store.texts.getValue(AREPAS))
        )
    }

    @Test
    fun `closing a deleted note empties the editor and says so`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)
        deleteOutside()

        viewModel.onIntent(intent = EditorIntent.OnCloseDeleted)
        runCurrent()

        assertEquals(
            expected = Triple(null, "", listOf<EditorEffect>(EditorEffect.Closed)),
            actual = Triple(viewModel.state.value.notePath, viewModel.buffer.text.toString(), effects)
        )
    }

    @Test
    fun `after a move in Notekea, edits are saved at the new path only`() = runEditorTest {
        open(path = AREPAS)
        val moved = RelativePath(value = "Recipes/Arepas reina.md")
        store.texts[moved] = store.texts.remove(AREPAS).orEmpty()
        scanner.files.remove(AREPAS)
        scanner.addNote(file = file(path = moved.value))

        viewModel.onIntent(intent = EditorIntent.OnNoteMoved(path = moved))
        type(text = "Edited after the move")
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()

        assertEquals(
            expected = "---\ntags: [recipe]\n---\nEdited after the move" to null,
            actual = store.texts[moved] to store.texts[AREPAS]
        )
    }

    @Test
    fun `the title renames the note after saving it, and the notes that link to it follow`() = runEditorTest {
        val effects = collectEffects()
        store.texts[WELCOME] = "Cook [[Arepas]]."
        open(path = AREPAS)
        type(text = "Saved before the rename")

        viewModel.onIntent(intent = EditorIntent.OnTitleCommit(name = "Arepas reina"))
        runCurrent()

        val renamed = RelativePath(value = "Recipes/Arepas reina.md")
        assertEquals(
            expected = listOf(
                "---\ntags: [recipe]\n---\nSaved before the rename",
                "Cook [[Arepas reina]]."
            ),
            actual = listOf(store.texts.getValue(AREPAS), store.texts.getValue(WELCOME))
        )
        assertEquals<Triple<Any?, Any?, Any?>>(
            expected = Triple(
                renamed,
                listOf(AREPAS to renamed),
                listOf<EditorEffect>(
                    EditorEffect.Renamed(
                        from = AREPAS,
                        to = renamed,
                        links = LinkUpdateResult(updated = listOf(WELCOME), skipped = emptyList())
                    )
                )
            ),
            actual = Triple(viewModel.state.value.notePath, operations.moves, effects)
        )
    }

    @Test
    fun `a refused title is explained until it is edited, and the same title changes nothing`() = runEditorTest {
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnTitleCommit(name = " Arepas "))
        viewModel.onIntent(intent = EditorIntent.OnTitleCommit(name = "a/b"))
        runCurrent()
        val refused = viewModel.state.value.titleError
        viewModel.onIntent(intent = EditorIntent.OnTitleErrorDismiss)

        assertEquals<Triple<Any?, Any?, Any?>>(
            expected = Triple(NotesError.InvalidName, null, emptyList<Pair<RelativePath, RelativePath>>()),
            actual = Triple(refused, viewModel.state.value.titleError, operations.moves)
        )
    }

    @Test
    fun `a save never brings back a note that is gone, it offers to restore it`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Edited")
        store.texts.remove(AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()

        assertEquals(
            expected = null to true,
            actual = store.texts[AREPAS] to viewModel.state.value.isDeleted
        )
    }
}

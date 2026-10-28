package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

/**
 * The editor over real use cases and fake ports. The open note is polled forever, so time only moves with
 * [advanceTimeBy] and [runCurrent], never `advanceUntilIdle`, and every test ends by cancelling the ViewModel's scope.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest : EditorViewModelTestBase() {
    @Test
    fun `an opened note shows its body, and edits are saved a second after the last keystroke`() = runEditorTest {
        open(path = AREPAS)
        val opened = viewModel.buffer.text.toString()

        type(text = "# Arepas reina\n")
        advanceTimeBy(delayTimeMillis = HALF_SECOND_MILLIS)
        val beforeTheDelay = store.texts.getValue(AREPAS)
        advanceTimeBy(delayTimeMillis = SECOND_MILLIS)
        runCurrent()

        assertEquals(
            expected = listOf("# Arepas\n", AREPAS_TEXT, "---\ntags: [recipe]\n---\n# Arepas reina\n"),
            actual = listOf(opened, beforeTheDelay, store.texts.getValue(AREPAS))
        )
        assertEquals(
            expected = SaveStatus.SAVED,
            actual = viewModel.state.value.saveStatus
        )
    }

    @Test
    fun `typing shows unsaved, and undoing every edit leaves nothing to save`() = runEditorTest {
        open(path = AREPAS)

        type(text = "# Changed\n")
        val unsaved = viewModel.state.value.saveStatus
        type(text = "# Arepas\n")
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
        runCurrent()

        assertEquals(
            expected = listOf(SaveStatus.UNSAVED, SaveStatus.NONE),
            actual = listOf(unsaved, viewModel.state.value.saveStatus)
        )
        assertEquals(
            expected = AREPAS_TEXT,
            actual = store.texts.getValue(AREPAS)
        )
    }

    @Test
    fun `save, opening another note and closing the window all save at once`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        type(text = "Saved by the shortcut")
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()
        val bySave = store.texts.getValue(AREPAS)
        type(text = "Saved before switching")
        open(path = WELCOME)
        val bySwitch = store.texts.getValue(AREPAS)
        type(text = "Saved before closing")
        viewModel.onIntent(intent = EditorIntent.OnFlushRequest)
        runCurrent()

        assertEquals(
            expected = listOf(
                "---\ntags: [recipe]\n---\nSaved by the shortcut",
                "---\ntags: [recipe]\n---\nSaved before switching",
                "Saved before closing"
            ),
            actual = listOf(bySave, bySwitch, store.texts.getValue(WELCOME))
        )
        assertEquals(
            expected = listOf<EditorEffect>(EditorEffect.Flushed),
            actual = effects
        )
    }

    @Test
    fun `the editor's own save is not taken for an outside change`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Mine")
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()

        scanner.addNote(file = file(path = AREPAS.value, modifiedOffsetMillis = 1L))
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)

        assertEquals(
            expected = false to "Mine",
            actual = viewModel.state.value.hasConflict to viewModel.buffer.text.toString()
        )
    }

    @Test
    fun `the folder above the title asks the window to show it in Files`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnFolderReveal)
        runCurrent()

        assertEquals(
            expected = listOf<EditorEffect>(EditorEffect.RevealFolder(path = RelativePath(value = "Recipes"))),
            actual = effects.filterIsInstance<EditorEffect.RevealFolder>()
        )
    }

    @Test
    fun `leaving the text saves at once`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Saved on focus loss")

        viewModel.onIntent(intent = EditorIntent.OnFocusLost)
        runCurrent()

        assertEquals(
            expected = "---\ntags: [recipe]\n---\nSaved on focus loss",
            actual = store.texts.getValue(AREPAS)
        )
    }

    @Test
    fun `a note that cannot be read shows the error until a retry opens it`() = runEditorTest {
        val missing = RelativePath(value = "Missing.md")
        open(path = missing)
        val failed = viewModel.state.value.loadError

        store.texts[missing] = "Found"
        viewModel.onIntent(intent = EditorIntent.OnRetryOpen)
        runCurrent()

        assertEquals(
            expected = Triple(NotesError.OperationFailed(path = missing.value), null, "Found"),
            actual = Triple(failed, viewModel.state.value.loadError, viewModel.buffer.text.toString())
        )
    }

    @Test
    fun `a refused save is shown and the next save succeeds`() = runEditorTest {
        open(path = AREPAS)
        store.refused += AREPAS

        type(text = "Locked")
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()
        val error = viewModel.state.value.saveError
        store.refused -= AREPAS
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()

        assertEquals(
            expected = Triple(NotesError.OperationFailed(path = AREPAS.value), null, SaveStatus.SAVED),
            actual = Triple(error, viewModel.state.value.saveError, viewModel.state.value.saveStatus)
        )
    }

    @Test
    fun `a focus request for the open note gives its text the keyboard at once`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnFocusRequest())
        runCurrent()

        assertEquals(
            expected = listOf<EditorEffect>(EditorEffect.FocusText),
            actual = effects
        )
    }

    @Test
    fun `a focus request for a note chosen in the quick switcher waits until it has opened`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnFocusRequest(path = WELCOME))
        runCurrent()
        val beforeOpening = effects.toList()
        open(path = WELCOME)

        assertEquals(
            expected = emptyList<EditorEffect>() to listOf<EditorEffect>(EditorEffect.FocusText),
            actual = beforeOpening to effects
        )
    }

    @Test
    fun `a focus request is dropped when another note opens instead`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnFocusRequest(path = WELCOME))
        runCurrent()
        open(path = RelativePath(value = "Other.md"))
        open(path = WELCOME)

        assertEquals(
            expected = emptyList<EditorEffect>(),
            actual = effects
        )
    }
}

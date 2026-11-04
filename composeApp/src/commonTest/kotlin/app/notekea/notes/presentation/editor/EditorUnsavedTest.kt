package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals

/** Text that could not be saved is never dropped when the note is left or the window closes. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorUnsavedTest : EditorViewModelTestBase() {
    @Test
    fun `leaving a note with a conflict keeps its text in a copy beside it, and says so`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)
        type(text = "Typed here")
        changeOutside(text = "---\ntags: [recipe]\n---\nFrom another app\n")

        open(path = WELCOME)

        assertEquals(
            expected = "---\ntags: [recipe]\n---\nTyped here" to
                EditorEffect.UnsavedKept(note = AREPAS, copy = COPY),
            actual = store.texts[COPY] to effects.filterIsInstance<EditorEffect.UnsavedKept>().single()
        )
    }

    @Test
    fun `closing the window with a failed save keeps the text, once however many times it is asked`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)
        store.refused += AREPAS
        type(text = "Typed here")

        repeat(times = 2) {
            viewModel.onIntent(intent = EditorIntent.OnFlushRequest)
            runCurrent()
        }

        assertEquals(
            expected = "---\ntags: [recipe]\n---\nTyped here" to 1,
            actual = store.texts[COPY] to effects.filterIsInstance<EditorEffect.UnsavedKept>().size
        )
    }

    @Test
    fun `when even the copy cannot be written, the text comes back with the note`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)
        store.refused += AREPAS
        operations.failure = NotesError.OperationFailed(path = AREPAS.value)
        type(text = "Typed here")

        open(path = WELCOME)
        open(path = AREPAS)

        assertEquals(
            expected = "Typed here" to EditorEffect.UnsavedKept(note = AREPAS, copy = null),
            actual = viewModel.buffer.text.toString() to effects.filterIsInstance<EditorEffect.UnsavedKept>().single()
        )
    }

    @Test
    fun `leaving a saved note keeps nothing`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)
        type(text = "Typed here")

        open(path = WELCOME)

        assertEquals(
            expected = "---\ntags: [recipe]\n---\nTyped here" to emptyList(),
            actual = store.texts[AREPAS] to effects.filterIsInstance<EditorEffect.UnsavedKept>()
        )
    }

    private companion object {
        val COPY = RelativePath(value = "Recipes/Arepas (unsaved).md")
    }
}

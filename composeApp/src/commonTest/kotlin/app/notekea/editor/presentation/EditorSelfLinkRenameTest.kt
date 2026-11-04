package app.notekea.editor.presentation

import app.notekea.core.notes.domain.RelativePath
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

/** A rename whose link updates rewrite the open note itself, because it links to itself. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorSelfLinkRenameTest : EditorViewModelTestBase() {
    @BeforeTest
    fun fileFollowsMoves() {
        operations.onMove = { from, to -> store.texts.remove(key = from)?.let { store.texts[to] = it } }
        store.texts[AREPAS] = SELF_LINK
    }

    @Test
    fun `the editor shows the rewritten link, and the next save is no conflict`() = runEditorTest {
        open(path = AREPAS)

        rename()
        assertEquals(expected = "See [[Reina]].\n", actual = viewModel.buffer.text.toString())

        typeAndSave(text = "See [[Reina]], typed.\n")
        assertEquals(
            expected = false to "See [[Reina]], typed.\n",
            actual = viewModel.state.value.hasConflict to store.texts[RENAMED]
        )
    }

    @Test
    fun `edits a save could not write stay on top of the rewritten file, with no conflict`() = runEditorTest {
        open(path = AREPAS)
        store.refused += AREPAS
        type(text = "See [[Arepas]], mine.\n")

        rename()
        assertEquals(expected = "See [[Reina]].\n", actual = store.texts[RENAMED])
        assertEquals(expected = "See [[Arepas]], mine.\n", actual = viewModel.buffer.text.toString())

        typeAndSave(text = "See [[Reina]], mine.\n")
        assertEquals(
            expected = false to "See [[Reina]], mine.\n",
            actual = viewModel.state.value.hasConflict to store.texts[RENAMED]
        )
    }

    @Test
    fun `a link update from the tree reaches the open note, whose next save is no conflict`() = runEditorTest {
        open(path = AREPAS)
        store.texts[AREPAS] = "See [[Reina]].\n"

        viewModel.onIntent(intent = EditorIntent.OnNoteRewritten(path = AREPAS))
        runCurrent()
        typeAndSave(text = "See [[Reina]], typed.\n")

        assertEquals(
            expected = false to "See [[Reina]], typed.\n",
            actual = viewModel.state.value.hasConflict to store.texts[AREPAS]
        )
    }

    @Test
    fun `a link update from the tree clears the conflict the watcher saw first`() = runEditorTest {
        open(path = AREPAS)
        type(text = "See [[Arepas]], mine.\n")
        changeOutside(text = "See [[Reina]].\n")

        viewModel.onIntent(intent = EditorIntent.OnNoteRewritten(path = AREPAS))
        runCurrent()

        assertEquals(
            expected = false to "See [[Arepas]], mine.\n",
            actual = viewModel.state.value.hasConflict to viewModel.buffer.text.toString()
        )
    }

    private fun TestScope.rename() {
        viewModel.onIntent(intent = EditorIntent.OnTitleCommit(name = "Reina"))
        runCurrent()
    }

    private fun TestScope.typeAndSave(
        text: String
    ) {
        type(text = text)
        advanceTimeBy(delayTimeMillis = SECOND_MILLIS + 1)
        runCurrent()
    }

    companion object {
        private const val SELF_LINK = "See [[Arepas]].\n"
        private val RENAMED = RelativePath(value = "Recipes/Reina.md")
    }
}

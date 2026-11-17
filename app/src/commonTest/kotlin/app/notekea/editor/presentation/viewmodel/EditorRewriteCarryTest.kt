package app.notekea.editor.presentation.viewmodel

import app.notekea.editor.presentation.model.EditorIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals

/** Notekea rewrites the open note while it holds unsaved edits (the bug hunt, D2). */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorRewriteCarryTest : EditorViewModelTestBase() {
    // The edits were rebased onto the rewritten file as they were, so their save wrote the old link back.
    @Test
    fun `a rewrite of the text the edits were made on is made on them too, and the save keeps both`() =
        runEditorTest {
            store.texts[AREPAS] = ORIGINAL
            open(path = AREPAS)
            type(text = "# Arepas, mine\n\nSee [[Menu]].\n")

            rewrite()

            assertEquals(
                expected = "# Arepas, mine\n\nSee [[Dinner]].\n",
                actual = viewModel
                    .buffer
                    .text
                    .toString()
            )

            advanceTimeBy(delayTimeMillis = SECOND_MILLIS + 1)
            runCurrent()

            assertEquals(
                expected = false to "$HEADER# Arepas, mine\n\nSee [[Dinner]].\n",
                actual = viewModel.state.value.hasConflict to store.texts[AREPAS]
            )
        }

    @Test
    fun `an edit to the rewritten link asks, keeping both the edit and the rewrite`() =
        runEditorTest {
            store.texts[AREPAS] = ORIGINAL
            open(path = AREPAS)
            type(text = "# Arepas\n\nSee [[Menus]].\n")

            rewrite()
            advanceTimeBy(delayTimeMillis = SECOND_MILLIS + 1)
            runCurrent()

            assertEquals(
                expected = Triple(true, "# Arepas\n\nSee [[Menus]].\n", REWRITTEN),
                actual = Triple(
                    viewModel.state.value.hasConflict,
                    viewModel
                        .buffer
                        .text
                        .toString(),
                    store.texts[AREPAS]
                )
            )
        }

    private fun TestScope.rewrite() {
        store.texts[AREPAS] = REWRITTEN

        viewModel.onIntent(
            intent = EditorIntent.OnNoteRewritten(
                path = AREPAS,
                original = ORIGINAL
            )
        )

        runCurrent()
    }

    companion object {
        private const val HEADER = "---\ntags: [recipe]\n---\n"
        private const val ORIGINAL = "$HEADER# Arepas\n\nSee [[Menu]].\n"
        private const val REWRITTEN = "$HEADER# Arepas\n\nSee [[Dinner]].\n"
    }
}

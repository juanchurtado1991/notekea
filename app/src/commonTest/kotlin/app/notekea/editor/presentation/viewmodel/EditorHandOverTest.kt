package app.notekea.editor.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import app.notekea.editor.presentation.model.EditorIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals

/** A note whose tab moves to the other pane: two editors over one disk, the source still saving. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorHandOverTest : EditorViewModelTestBase() {
    @Test
    fun `the other pane opens the note with the last keystrokes, and saves it without a false conflict`() =
        runEditorTest {
            val destination = fixture.create()

            try {
                open(path = AREPAS)
                type(text = "Typed just before the move\n")

                backgroundScope.launch {
                    saveInOtherEditors(
                        path = AREPAS,
                        editor = destination,
                        editors = listOf(viewModel, destination)
                    )

                    destination.onIntent(intent = EditorIntent.OnOpen(path = AREPAS))
                }

                runCurrent()
                viewModel.onIntent(intent = EditorIntent.OnOpen(path = WELCOME))
                runCurrent()

                assertEquals(
                    expected = "Typed just before the move\n",
                    actual = destination
                        .buffer
                        .text
                        .toString()
                )

                advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
                runCurrent()

                assertEquals(
                    expected = false,
                    actual = destination.state.value.hasConflict
                )
            } finally {
                destination.viewModelScope.cancel()
            }
        }

    @Test
    fun `an editor that saved first and moved on is not waited for, and one on another note stays out of it`() =
        runEditorTest {
            val destination = fixture.create()

            try {
                open(path = WELCOME)

                backgroundScope.launch {
                    saveInOtherEditors(
                        path = AREPAS,
                        editor = destination,
                        editors = listOf(viewModel, destination)
                    )

                    destination.onIntent(intent = EditorIntent.OnOpen(path = AREPAS))
                }

                runCurrent()

                assertEquals(
                    expected = AREPAS,
                    actual = destination.state.value.notePath
                )
            } finally {
                destination.viewModelScope.cancel()
            }
        }
}

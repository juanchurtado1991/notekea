package app.notekea.editor.presentation.view

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A passage selected only to be read, as a source cited in the chat is (the developer's request of 2026-11-14): its
 * lines stay drawn as the note reads, their Markdown marks hidden, until the person selects something else.
 */
class ShownPassageTest {
    private fun shownText(
        selection: TextRange,
        passage: IntRange?
    ): String {
        val page = EditorPageState(
            buffer = TextFieldState(
                initialText = BODY,
                initialSelection = selection
            ),
            listState = LazyListState(),
            focusRequester = FocusRequester(),
            isSource = { false },
            shownPassage = { passage }
        )

        page.hasFocus = true

        return page.shown.text
    }

    @Test
    fun `the passage shown keeps its marks hidden, and any other selection edits its lines as before`() {
        val passage = TextRange(
            start = 2,
            end = 10
        )

        assertEquals(
            expected = listOf(READ, BODY, BODY),
            actual = listOf(
                shownText(
                    selection = passage,
                    passage = 2..9
                ),
                shownText(
                    selection = passage,
                    passage = null
                ),
                shownText(
                    selection = TextRange(index = 4),
                    passage = 2..9
                )
            )
        )
    }

    private companion object {
        const val BODY = "A **bold** word"
        const val READ = "A bold word"
    }
}

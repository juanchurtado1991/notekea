package app.notekea.editor.presentation.viewmodel

import androidx.compose.ui.text.TextRange
import app.notekea.editor.presentation.model.EditorIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals

/** Text pasted with the shortcut, which the text field's own rules never see. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorPasteTest : EditorViewModelTestBase() {
    @Test
    fun `text pasted in a table's cell keeps the row whole, as typing it would`() = runEditorTest {
        putOnDisk(
            path = TASKS,
            text = TABLE
        )

        open(path = TASKS)
        fixture.clipboard.text = "one\r\ntwo | three"

        viewModel.buffer.edit { selection = TextRange(index = TABLE.indexOf(string = "x") + 1) }

        viewModel.onIntent(intent = EditorIntent.OnPaste)
        runCurrent()

        assertEquals(
            expected = "| A |\n| --- |\n| xone two \\| three |\n",
            actual = viewModel
                .buffer
                .text
                .toString()
        )
    }

    @Test
    fun `text pasted outside a table keeps its line breaks and pipes`() = runEditorTest {
        open(path = WELCOME)
        fixture.clipboard.text = "one\ntwo | three"

        viewModel.buffer.edit { selection = TextRange(index = length) }

        viewModel.onIntent(intent = EditorIntent.OnPaste)
        runCurrent()

        assertEquals(
            expected = "# Welcome\none\ntwo | three",
            actual = viewModel
                .buffer
                .text
                .toString()
        )
    }

    private companion object {
        const val TABLE = "| A |\n| --- |\n| x |\n"
    }
}

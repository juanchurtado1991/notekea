package app.notekea.editor.presentation.markdown

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals

/** The text field shows a window of the note: what it does to the text outside its window stops at the window. */
@OptIn(ExperimentalTestApi::class)
class KeepInWindowTest {
    private val window = TextRange(
        start = TEXT.indexOf(string = "two"),
        end = TEXT.indexOf(string = "two") + "two".length
    )

    @Test
    fun `a selection that leaves the window stops at its edges`() = runComposeUiTest {
        val buffer = buffer()

        setContent {
            BasicTextField(
                state = buffer,
                inputTransformation = KeepInWindow { window }
            )
        }

        onNode(matcher = hasSetTextAction()).performTextInputSelection(
            selection = TextRange(
                start = 0,
                end = TEXT.length
            )
        )

        assertEquals(
            expected = window,
            actual = buffer.selection
        )
    }

    @Test
    fun `a change that reaches outside the window deletes only its part of the window`() =
        runComposeUiTest {
            val buffer = buffer()

            setContent {
                BasicTextField(
                    state = buffer,
                    inputTransformation = KeepInWindow { window }
                )
            }

            onNode(matcher = hasSetTextAction()).performTextReplacement(text = "")

            assertEquals(
                expected = "one\n\n\n\nthree",
                actual = buffer.text.toString()
            )

            assertEquals(
                expected = TextRange(index = window.min),
                actual = buffer.selection
            )
        }

    @Test
    fun `text typed inside the window goes in`() = runComposeUiTest {
        val buffer = buffer()

        setContent {
            BasicTextField(
                state = buffer,
                inputTransformation = KeepInWindow { window }
            )
        }

        onNode(matcher = hasSetTextAction()).performTextInput(text = "2")

        assertEquals(
            expected = "one\n\n2two\n\nthree",
            actual = buffer.text.toString()
        )
    }

    private fun buffer() = TextFieldState(
        initialText = TEXT,
        initialSelection = TextRange(index = window.min)
    )

    companion object {
        private const val TEXT = "one\n\ntwo\n\nthree"
    }
}

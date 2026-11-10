package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.presentation.model.EditorState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The cursor under its block's text, past a blank line: the block is drawn as it reads again (a list, a callout, a
 * picture), and what is typed goes under it. Found in the smoke of One editing mode: the block stayed as written.
 */
@OptIn(ExperimentalTestApi::class)
class EditorUnderBlockTest {
    private val buffer = TextFieldState(
        initialText = BODY,
        initialSelection = TextRange(index = BODY.length)
    )

    @Test
    fun `two lines under a callout it is drawn, and the field holds only the lines under it`() =
        runComposeUiTest {
            show()

            assertEquals(
                expected = "",
                actual = fieldText()
            )

            assertTrue(actual = drawn(text = "Rest the dough").fetchSemanticsNodes().isNotEmpty())
        }

    @Test
    fun `one line under it the callout is still text, as on its own lines`() = runComposeUiTest {
        show()

        buffer.edit { selection = TextRange(index = BODY.length - 1) }
        waitForIdle()

        assertEquals(
            expected = BODY.substringAfter(delimiter = "\n\n"),
            actual = fieldText()
        )
    }

    @Test
    fun `what is typed under the drawn block goes after it, and a click on the block makes it text again`() =
        runComposeUiTest {
            show()

            field().performTextInput(text = "Next")
            waitForIdle()

            assertEquals(
                expected = BODY + "Next" to "Next",
                actual = buffer.text.toString() to fieldText()
            )

            drawn(text = "Rest the dough")[0].performMouseInput { click() }
            waitForIdle()

            // The cursor is in the callout's own lines: before the blank line that ends it.
            assertTrue(actual = buffer.selection.start <= BODY.trimEnd().length)
        }

    private fun ComposeUiTest.show() {
        setContent {
            NotekeaTheme(darkTheme = false) {
                EditorContent(
                    state = EditorState(notePath = RelativePath(value = NOTE)),
                    buffer = buffer,
                    platformConventions = PreviewPlatformConventions.linux,
                    onIntent = {}
                )
            }
        }
    }

    /** The texts drawn that hold [text]: the field's is the whole note's, so it does not count. */
    private fun ComposeUiTest.drawn(text: String) = onAllNodes(
        matcher = hasText(
            text = text,
            substring = true
        ) and !hasSetTextAction()
    )

    /** The body's field: the note's title is the first text field of the editor. */
    private fun ComposeUiTest.field() = onAllNodes(matcher = hasSetTextAction())[1]

    /** The text the field lays out: its semantics' text is the note's, not what is shown of it. */
    private fun ComposeUiTest.fieldText(): String? {
        val layouts = mutableListOf<TextLayoutResult>()

        field()
            .fetchSemanticsNode()
            .config
            .getOrNull(key = SemanticsActions.GetTextLayoutResult)
            ?.action
            ?.invoke(layouts)

        return layouts
            .firstOrNull()
            ?.layoutInput
            ?.text
            ?.text
    }

    companion object {
        private const val BODY = "# Arepas\n\n> [!tip] Tip\n> Rest the dough\n\n"
        private const val NOTE = "Recipes/Arepas.md"
    }
}

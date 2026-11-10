package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.presentation.model.EditorState
import kotlin.test.Test
import kotlin.test.assertEquals

/** What the text field shows of the block the cursor is in: marks on the cursor's line only, all of them in Source. */
@OptIn(ExperimentalTestApi::class)
class EditorHiddenMarksTest {
    @Test
    fun `the field hides the marks of every line while the cursor is away`() = runComposeUiTest {
        show(isSource = false)

        assertEquals(
            expected = "Made with corn flour\nand water, see the dough\n",
            actual = fieldText()
        )
    }

    @Test
    fun `the cursor's line shows its marks, the block's other lines stay finished`() =
        runComposeUiTest {
            show(isSource = false)

            field().performSemanticsAction(key = SemanticsActions.RequestFocus)
            waitForIdle()

            assertEquals(
                expected = "Made with **corn flour**\nand water, see the dough\n",
                actual = fieldText()
            )
        }

    @Test
    fun `Source shows the whole note as written`() = runComposeUiTest {
        show(isSource = true)

        assertEquals(
            expected = BODY,
            actual = fieldText()
        )
    }

    private fun ComposeUiTest.show(isSource: Boolean) {
        setContent {
            NotekeaTheme(darkTheme = false) {
                EditorContent(
                    state = EditorState(
                        notePath = RelativePath(value = NOTE),
                        isSourceMode = isSource
                    ),
                    buffer = TextFieldState(
                        initialText = BODY,
                        initialSelection = TextRange(index = BODY.indexOf(string = "Made"))
                    ),
                    platformConventions = PreviewPlatformConventions.linux,
                    onIntent = {}
                )
            }
        }
    }

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
        private const val BODY =
            "# Arepas\n\nMade with **corn flour**\nand *water*, see [[Recipes/Masa|the dough]]\n\n- one\n"
        private const val NOTE = "Recipes/Arepas.md"
    }
}

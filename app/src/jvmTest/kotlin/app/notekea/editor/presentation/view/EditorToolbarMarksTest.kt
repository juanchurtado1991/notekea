package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextRange
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.presentation.model.EditorState
import kotlin.test.Test

/** The toolbar marks the buttons of the formats the cursor is in, while the text has the keyboard. */
@OptIn(ExperimentalTestApi::class)
class EditorToolbarMarksTest {
    private val buffer = TextFieldState(
        initialText = BODY,
        initialSelection = TextRange(index = BODY.indexOf(string = "corn"))
    )

    @Test
    fun `no button is marked while the text has no keyboard`() = runComposeUiTest {
        show()

        onNodeWithContentDescription(label = "Bold").assertIsNotSelected()
    }

    @Test
    fun `the cursor in bold words of a checklist marks Bold and Checklist, and they follow the cursor`() =
        runComposeUiTest {
            show()

            // The body's field: the note's title is the first text field of the editor.
            onAllNodes(matcher = hasSetTextAction())[1]
                .performSemanticsAction(key = SemanticsActions.RequestFocus)

            waitForIdle()

            onNodeWithContentDescription(label = "Bold").assertIsSelected()
            onNodeWithContentDescription(label = "Checklist").assertIsSelected()
            onNodeWithContentDescription(label = "Italic").assertIsNotSelected()

            buffer.edit { selection = TextRange(index = BODY.indexOf(string = "water")) }
            waitForIdle()

            onNodeWithContentDescription(label = "Bold").assertIsNotSelected()
            onNodeWithContentDescription(label = "Checklist").assertIsNotSelected()
            onNodeWithContentDescription(label = "Italic").assertIsSelected()
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

    companion object {
        private const val BODY = "- [ ] Buy **corn flour**\n\nMix it with *warm water*.\n"
        private const val NOTE = "Recipes/Arepas.md"
    }
}

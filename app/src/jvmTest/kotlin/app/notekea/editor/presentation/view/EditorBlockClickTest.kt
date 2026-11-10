package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextRange
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.presentation.model.EditorState
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A click on a drawn block puts the cursor on the line clicked, also when the block is drawn with another height than
 * the text field gives it: a list's items are drawn further apart than the field writes its lines.
 */
@OptIn(ExperimentalTestApi::class)
class EditorBlockClickTest {
    @Test
    fun `a click on a line of a drawn list puts the cursor on that line`() = runComposeUiTest {
        val buffer = TextFieldState(
            initialText = BODY,
            initialSelection = TextRange(index = 0)
        )

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

        onNodeWithText(text = CLICKED).performMouseInput { click() }
        waitForIdle()

        val line = BODY.indexOf(string = CLICKED).let {
            it..it + CLICKED.length
        }

        assertTrue(
            actual = buffer.selection.collapsed && buffer.selection.start in line,
            message = "The cursor is at ${buffer.selection}, the line clicked is $line"
        )
    }

    private companion object {
        const val CLICKED = "Serve them warm"
        const val NOTE = "Recipes/Arepas.md"

        const val BODY =
            "# Arepas\n\n- Mix the corn flour\n- Add the warm water\n- Knead the dough\n- Rest it\n- Shape the arepas\n" +
                "- Grill both sides\n- $CLICKED\n- Fill with cheese\n"
    }
}

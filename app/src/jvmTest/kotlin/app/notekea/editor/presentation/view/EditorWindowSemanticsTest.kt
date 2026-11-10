package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextRange
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.presentation.model.EditorState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The editor's text field shows only the cursor's block, but what it tells a screen reader and the Auto-Test is the
 * whole note: its text, and a selection counted from the note's start.
 */
@OptIn(ExperimentalTestApi::class)
class EditorWindowSemanticsTest {
    @Test
    fun `the field reads, selects and replaces the whole note`() = runComposeUiTest {
        val buffer = TextFieldState(
            initialText = BODY,
            initialSelection = TextRange(index = 0)
        )

        setContent {
            NotekeaTheme(darkTheme = false) {
                EditorContent(
                    state = EditorState(notePath = RelativePath(value = "Arepas.md")),
                    buffer = buffer,
                    platformConventions = PreviewPlatformConventions.linux,
                    onIntent = {}
                )
            }
        }

        val field =
            onNode(matcher = hasSetTextAction() and hasContentDescription(value = "Note text"))

        val serve = BODY.indexOf(string = SERVE)

        assertEquals(
            expected = BODY,
            actual = field
                .fetchSemanticsNode()
                .config
                .getOrNull(key = SemanticsProperties.EditableText)
                ?.text
        )

        field.performTextInputSelection(selection = TextRange(index = serve))

        assertEquals(
            expected = TextRange(index = serve),
            actual = buffer.selection
        )

        assertEquals(
            expected = TextRange(index = serve),
            actual = field
                .fetchSemanticsNode()
                .config
                .getOrNull(key = SemanticsProperties.TextSelectionRange)
        )

        field.performTextInput(text = "Then: ")

        assertEquals(
            expected = BODY.replace(
                oldValue = SERVE,
                newValue = "Then: $SERVE"
            ),
            actual = buffer.text.toString()
        )

        field.performTextReplacement(text = "# Cachapas\r\n\r\nSweet corn.")

        assertEquals(
            expected = "# Cachapas\n\nSweet corn.",
            actual = buffer.text.toString()
        )
    }

    companion object {
        private const val SERVE = "Serve hot."
        private const val BODY = "# Arepas\n\nMix the corn flour.\n\nRest the dough.\n\n$SERVE"
    }
}

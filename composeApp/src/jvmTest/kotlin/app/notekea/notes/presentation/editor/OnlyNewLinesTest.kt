package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Text the field takes by itself, as its own Paste menu and Shift+Insert do, skips the editor's Mod+V (audit 32). */
@OptIn(ExperimentalTestApi::class)
class OnlyNewLinesTest {
    @Test
    fun `Windows breaks that reach the field by themselves keep only their new line`() = runComposeUiTest {
        val buffer = TextFieldState(initialText = "Start\n")
        setContent { BasicTextField(state = buffer, inputTransformation = OnlyNewLines) }

        onNode(matcher = hasSetTextAction()).performTextInput(text = "one\r\ntwo\r\n")

        assertEquals(expected = "Start\none\ntwo\n", actual = buffer.text.toString())
    }
}

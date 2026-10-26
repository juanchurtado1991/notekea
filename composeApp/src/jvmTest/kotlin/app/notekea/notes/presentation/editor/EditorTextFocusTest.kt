package app.notekea.notes.presentation.editor

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The focus requester given to the editor reaches its text (1.6 smoke: the keyboard did not come back to the note
 * after the quick switcher closed). The text takes focus, then hands it to another element, which it reports as a
 * focus loss: only a focused text can lose focus.
 */
class EditorTextFocusTest {
    @Test
    fun `the editor's focus requester gives the keyboard to the note's text`() {
        val intents = mutableListOf<EditorIntent>()
        val text = FocusRequester()
        val elsewhere = FocusRequester()
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT) {
                NotekeaTheme(darkTheme = false) {
                    Column {
                        Box(modifier = Modifier.size(size = OTHER_SIZE_DP.dp).focusRequester(elsewhere).focusable())
                        EditorContent(
                            state = EditorState(notePath = RelativePath(value = NOTE)),
                            buffer = TextFieldState(initialText = BODY),
                            platformConventions = PreviewPlatformConventions.linux,
                            onIntent = { intents += it },
                            textFocusRequester = text
                        )
                    }
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                text.requestFocus()
                scene.render(nanoTime = FRAME_NANOS)
                elsewhere.requestFocus()
                scene.render(nanoTime = 2 * FRAME_NANOS)
            }
        }

        assertEquals(
            expected = listOf<EditorIntent>(EditorIntent.OnFocusLost),
            actual = intents
        )
    }

    companion object {
        private const val BODY = "# Arepas\n\nMix the corn flour.\n"
        private const val FRAME_NANOS = 16_000_000L
        private const val NOTE = "Recipes/Arepas.md"
        private const val OTHER_SIZE_DP = 8
        private const val WINDOW_HEIGHT = 600
        private const val WINDOW_WIDTH = 900
    }
}

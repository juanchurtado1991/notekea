package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextRange
import app.notekea.core.notes.domain.OutlineHeading
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.presentation.model.EditorState
import app.notekea.editor.presentation.sidepanel.OutlineTracker
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A heading chosen in the outline is scrolled to the top of the editor, below the toolbar, and its line takes the
 * cursor and the keyboard. The list keeps the cursor below its toolbar by itself: the editor counted the toolbar a
 * second time, so the page moved again and the outline marked the heading before the one chosen.
 */
@OptIn(ExperimentalTestApi::class)
class EditorOutlineRevealTest {
    @Test
    fun `a heading chosen in the outline stays the current one once its line takes the keyboard`() =
        runComposeUiTest {
            val buffer = TextFieldState(
                initialText = BODY,
                initialSelection = TextRange(index = 0)
            )

            val tracker = OutlineTracker()
            val focus = FocusRequester()

            setContent {
                NotekeaTheme(darkTheme = false) {
                    EditorContent(
                        state = EditorState(
                            notePath = RelativePath(value = NOTE),
                            outline = OUTLINE
                        ),
                        buffer = buffer,
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = {},
                        textFocusRequester = focus,
                        outlineTracker = tracker
                    )
                }
            }

            waitForIdle()

            val chosen = OUTLINE[CHOSEN]

            runOnIdle {
                buffer.edit {
                    selection = TextRange(index = chosen.offset + chosen.text.length + MARK.length)
                }

                tracker.reveal(heading = chosen)
                focus.requestFocus()
            }

            waitForIdle()

            assertEquals(
                expected = CHOSEN,
                actual = tracker.current
            )
        }

    private companion object {
        const val CHOSEN = 6
        const val MARK = "## "
        const val NOTE = "Recipes/Arepas.md"
        const val SECTIONS = 12

        val BODY = (0 until SECTIONS)
            .joinToString(separator = "") { section ->
                "${MARK}Section $section\n\nMix the corn flour.\n\nAdd the warm water.\n\nKnead the dough.\n\n"
            }

        val OUTLINE = (0 until SECTIONS).map { section ->
            val text = "Section $section"

            OutlineHeading(
                level = 2,
                text = text,
                offset = BODY.indexOf(string = MARK + text),
                depth = 0
            )
        }
    }
}

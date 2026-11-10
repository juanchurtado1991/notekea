package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextRange
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.domain.EmbedFinder
import app.notekea.editor.presentation.attachments.AttachmentView
import app.notekea.editor.presentation.model.EditorState
import kotlin.test.Test

/**
 * The cursor's block is the text field, which shows an embedded file and a query block as written: the file and the
 * query's table are drawn under it, and once more nowhere when the cursor leaves the block.
 */
@OptIn(ExperimentalTestApi::class)
class EditorWindowEmbedsTest {
    @Test
    fun `a file embedded in the cursor's block is drawn under the field, once`() =
        runComposeUiTest {
            val buffer = TextFieldState(
                initialText = FILE_NOTE,
                initialSelection = TextRange(index = 0)
            )

            val file = AttachmentView(
                embed = EmbedFinder().embeds(text = FILE_NOTE).single(),
                kind = AttachmentKind.OTHER,
                path = RelativePath(value = "Recipes/$FILE")
            )

            setContent {
                NotekeaTheme(darkTheme = false) {
                    EditorContent(
                        state = EditorState(
                            notePath = NOTE,
                            attachments = listOf(file)
                        ),
                        buffer = buffer,
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = {}
                    )
                }
            }

            onAllNodes(matcher = hasText(text = FILE)).assertCountEquals(expectedSize = 1)

            buffer.edit { selection = TextRange(index = FILE_NOTE.length) }
            waitForIdle()

            onAllNodes(matcher = hasText(text = FILE)).assertCountEquals(expectedSize = 1)
        }

    @Test
    fun `a query block the cursor is in keeps its table under the field`() = runComposeUiTest {
        val buffer = TextFieldState(
            initialText = QUERY_NOTE,
            initialSelection = TextRange(index = 0)
        )

        setContent {
            NotekeaTheme(darkTheme = false) {
                CompositionLocalProvider(
                    LocalQueryBlock provides { yaml, _ -> Text(text = TABLE + yaml.trim()) }
                ) {
                    EditorContent(
                        state = EditorState(notePath = NOTE),
                        buffer = buffer,
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = {}
                    )
                }
            }
        }

        onAllNodes(matcher = hasText(text = TABLE + YAML)).assertCountEquals(expectedSize = 1)
    }

    companion object {
        private const val FILE = "menu.pdf"
        private const val FILE_NOTE = "The menu:\n![[$FILE]]\n\nEnd."
        private const val TABLE = "Table of "
        private const val YAML = "from: notes"
        private const val QUERY_NOTE = "```query\n$YAML\n```\n\nEnd."
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}

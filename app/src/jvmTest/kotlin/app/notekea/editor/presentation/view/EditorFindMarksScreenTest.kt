package app.notekea.editor.presentation.view

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.use
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.presentation.find.FindState
import app.notekea.editor.presentation.find.NoteSearch
import app.notekea.editor.presentation.model.EditorHeaderData
import app.notekea.editor.presentation.model.EditorState
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Find marks its matches in the blocks drawn as Read mode draws them, not only in the cursor's block, which is the
 * text field. The page is rendered headless, with Find and without it, and compared.
 */
class EditorFindMarksScreenTest {
    @Test
    fun `a match in a block the cursor is not in is marked`() {
        val matches = NoteSearch().matches(
            text = BODY,
            query = QUERY
        )

        val plain = rows(find = null)

        val found = rows(
            find = FindState(
                query = QUERY,
                matches = matches,
                current = 0
            )
        )

        val changed = plain.indices.filter {
            plain[it] != found[it]
        }

        // Without marks in the drawn blocks only the cursor's line changes, which is one line tall.
        assertTrue(
            actual = changed.isNotEmpty() && changed.last() - changed.first() > BLOCKS_APART_PX,
            message = "The rows that changed are $changed"
        )
    }

    /** The page's rows of pixels, with the cursor on the first match and [find] open or not. */
    private fun rows(find: FindState?): List<List<Int>> {
        val first = BODY.indexOf(string = QUERY)

        val page = EditorPageState(
            buffer = TextFieldState(
                initialText = BODY,
                initialSelection = TextRange(index = first)
            ),
            listState = LazyListState(),
            focusRequester = FocusRequester(),
            isSource = { false }
        )

        val pixels = IntArray(size = WINDOW_WIDTH * WINDOW_HEIGHT)

        UiThread.run {
            ImageComposeScene(
                width = WINDOW_WIDTH,
                height = WINDOW_HEIGHT
            ) {
                NotekeaTheme(darkTheme = false) {
                    EditorList(
                        state = EditorState(
                            notePath = RelativePath(value = "Recipes/Arepas.md"),
                            find = find
                        ),
                        page = page,
                        header = EditorHeaderData(dayNeighbors = null),
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = {}
                    )
                }
            }.use { scene ->
                repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) }

                scene
                    .render(nanoTime = SETTLE_FRAMES * FRAME_NANOS)
                    .toComposeImageBitmap()
                    .readPixels(buffer = pixels)
            }
        }

        return pixels.toList().chunked(size = WINDOW_WIDTH)
    }

    companion object {
        private const val BLOCKS_APART_PX = 60
        private const val BODY =
            "# Arepas\n\nMix the corn flour.\n\nRest the dough.\n\nServe with corn."
        private const val FRAME_NANOS = 160_000_000L
        private const val QUERY = "corn"
        private const val SETTLE_FRAMES = 4
        private const val WINDOW_HEIGHT = 600
        private const val WINDOW_WIDTH = 900
    }
}

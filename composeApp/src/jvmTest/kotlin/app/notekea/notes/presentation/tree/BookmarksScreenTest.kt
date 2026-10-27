package app.notekea.notes.presentation.tree

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.Bookmarks
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.editor.EditorContent
import app.notekea.notes.presentation.editor.EditorIntent
import app.notekea.notes.presentation.editor.EditorState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Bookmarks on screen (`ROADMAP.md` 1.13): the section above the tree folds, a bookmark opens its entry, one dragged
 * down a row moves there, a gone one is removed with its ✕, and the star beside the note's title toggles its bookmark.
 */
class BookmarksScreenTest {
    private val base = TreePreviewData.state()
    private val missing = RelativePath(value = "Old plan.md")
    private var frame = 0L

    @Test
    fun `the section folds, a bookmark opens, moves when dragged and goes with its cross, and the star toggles`() {
        val treeIntents = mutableListOf<VaultTreeIntent>()
        val editorIntents = mutableListOf<EditorIntent>()
        UiThread.run {
            scene(treeIntents = treeIntents, editorIntents = editorIntents).use { scene ->
                settle(scene = scene)
                listOf(HEADER, FIRST_BOOKMARK, MISSING_REMOVE, STAR).forEach { click(scene = scene, position = it) }
                drag(scene = scene, from = FIRST_BOOKMARK, to = SECOND_BOOKMARK)
            }
        }

        assertEquals(
            expected = listOf<VaultTreeIntent>(
                BookmarkIntent.OnSectionToggle,
                BookmarkIntent.OnClick(path = base.rows[2].path),
                BookmarkIntent.OnRemove(path = missing),
                BookmarkIntent.OnMove(from = 0, to = 1)
            ) to listOf<EditorIntent>(EditorIntent.OnBookmarkToggle),
            actual = treeIntents.filterIsInstance<BookmarkIntent>() to
                editorIntents.filter { it == EditorIntent.OnBookmarkToggle }
        )
    }

    private fun scene(
        treeIntents: MutableList<VaultTreeIntent>,
        editorIntents: MutableList<EditorIntent>
    ) = ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
        NotekeaTheme(darkTheme = false) {
            VaultTreeContent(
                state = base.copy(bookmarks = Bookmarks(paths = listOf(base.rows[2].path, base.rows[0].path, missing))),
                vaultName = TreePreviewData.VAULT_NAME,
                onIntent = { treeIntents += it },
                onSwitchVault = {},
                onSearch = {}
            ) {
                EditorContent(
                    state = EditorState(notePath = RelativePath(value = NOTE)),
                    buffer = TextFieldState(initialText = BODY),
                    platformConventions = PreviewPlatformConventions.linux,
                    onIntent = { editorIntents += it },
                    isBookmarked = true
                )
            }
        }
    }

    private fun click(
        scene: ImageComposeScene,
        position: Offset
    ) {
        scene.sendPointerEvent(eventType = PointerEventType.Move, position = position)
        scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
        scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
        settle(scene = scene)
    }

    private fun drag(
        scene: ImageComposeScene,
        from: Offset,
        to: Offset
    ) {
        val pressed = PointerButtons(isPrimaryPressed = true)
        scene.sendPointerEvent(eventType = PointerEventType.Move, position = from)
        scene.sendPointerEvent(eventType = PointerEventType.Press, position = from, buttons = pressed)
        (1..DRAG_STEPS).forEach { step ->
            val position = from + (to - from) * (step.toFloat() / DRAG_STEPS)
            scene.sendPointerEvent(eventType = PointerEventType.Move, position = position, buttons = pressed)
            settle(scene = scene)
        }
        scene.sendPointerEvent(eventType = PointerEventType.Release, position = to, buttons = PointerButtons())
        settle(scene = scene)
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val BODY = "Plant in March.\n"
        private const val DRAG_STEPS = 8
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 520
        private const val NOTE = "Projects/Garden/Tomato plan.md"
        private const val SETTLE_FRAMES = 4
        private const val WIDTH = 1100
        private val FIRST_BOOKMARK = Offset(x = 140f, y = 93f)
        private val HEADER = Offset(x = 110f, y = 65f)
        private val MISSING_REMOVE = Offset(x = 354f, y = 149f)
        private val SECOND_BOOKMARK = Offset(x = 140f, y = 121f)
        private val STAR = Offset(x = 1053f, y = 66f)
    }
}

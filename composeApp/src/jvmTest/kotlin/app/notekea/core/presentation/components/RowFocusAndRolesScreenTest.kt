package app.notekea.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.keyDown
import app.notekea.core.presentation.nodes
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.core.notes.domain.FrontmatterContent
import app.notekea.core.notes.domain.RelativePath
import app.notekea.connections.presentation.ConnectionActions
import app.notekea.editor.presentation.EditorPreviewData
import app.notekea.editor.presentation.EditorState
import app.notekea.editor.presentation.PropertiesView
import app.notekea.connections.presentation.RelatedList
import app.notekea.connections.presentation.RelatedNotesState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Rows with a click show the focus border when Tab reaches them, read in pixels at their left edge; chevrons and fold
 * titles are read as buttons, and a checkbox says its state from the resources.
 */
class RowFocusAndRolesScreenTest {
    private var frame = 0L

    @Test
    fun `an action row draws the focus border`() {
        val pixel = edgePixelFocusedAt(text = ACTION, tabs = 1) {
            NotekeaActionRow(icon = NotekeaIcons.Find, label = ACTION, keys = emptyList(), onClick = {})
        }

        assertEquals(expected = true, actual = pixel)
    }

    @Test
    fun `an already linked row draws the focus border`() {
        val pixel = edgePixelFocusedAt(text = LINKED, tabs = 2) {
            RelatedList(
                state = RelatedNotesState(
                    linked = listOf(RelativePath(value = "$LINKED.md")),
                    isLinkedOpen = true,
                    isIndexReady = true
                ),
                onIntent = {},
                actions = ConnectionActions(onOpen = {}, onReveal = { _, _ -> }, onOpenBeside = {}, onInsertLink = {})
            )
        }

        assertEquals(expected = true, actual = pixel)
    }

    @Test
    fun `a tree chevron and the properties' fold are read as buttons`() {
        var buttons = 0
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    Column {
                        NotekeaTreeItem(
                            label = LINKED,
                            depth = 0,
                            onClick = {},
                            icon = null,
                            isExpanded = false,
                            onToggleExpanded = {}
                        )
                        PropertiesView(
                            state = EditorState(
                                notePath = EditorPreviewData.path,
                                properties = FrontmatterContent.Invalid(line = 1),
                                isPropertiesExpanded = false
                            ),
                            onIntent = {}
                        )
                    }
                }
            }.use { scene ->
                settle(scene = scene)
                buttons = scene.nodes().count { it.config.getOrNull(SemanticsProperties.Role) == Role.Button }
            }
        }

        // The row, its chevron and the properties' title.
        assertEquals(expected = 3, actual = buttons)
    }

    @Test
    fun `a checkbox says its state`() {
        val said = mutableListOf<String?>()
        UiThread.run {
            listOf(true, false).forEach { checked ->
                ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                    NotekeaTheme(darkTheme = false) { NotekeaCheckbox(checked = checked, onClick = {}, label = ACTION) }
                }.use { scene ->
                    settle(scene = scene)
                    said += scene.nodes().firstNotNullOf { it.config.getOrNull(SemanticsProperties.StateDescription) }
                }
            }
        }

        assertEquals(expected = listOf<String?>("checked", "not checked"), actual = said)
    }

    /** Whether, after [tabs] presses of Tab, the left edge of the row showing [text] is in the theme's primary color. */
    private fun edgePixelFocusedAt(
        text: String,
        tabs: Int,
        content: @Composable () -> Unit
    ): Boolean {
        var primary = Color.Unspecified
        var isPrimary = false
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    val color = MaterialTheme.colorScheme.primary
                    SideEffect { primary = color }
                    content()
                }
            }.use { scene ->
                settle(scene = scene)
                repeat(times = tabs) {
                    scene.sendKeyEvent(event = keyDown(key = Key.Tab))
                    settle(scene = scene)
                }
                val row = scene.nodes().first { node ->
                    node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == text } == true
                }.boundsInRoot
                isPrimary = scene.pixelAt(position = Offset(x = 0f, y = row.center.y)) == primary.toArgb()
            }
        }
        return isPrimary
    }

    private fun ImageComposeScene.pixelAt(
        position: Offset
    ): Int {
        val pixels = IntArray(size = 1)
        render(nanoTime = (frame++) * FRAME_NANOS).toComposeImageBitmap().readPixels(
            buffer = pixels,
            startX = position.x.toInt(),
            startY = position.y.toInt(),
            width = 1,
            height = 1
        )
        return pixels.single()
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val ACTION = "Find a note"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 300
        private const val LINKED = "Weekly menu"
        private const val SETTLE_FRAMES = 4
        private const val WIDTH = 320
    }
}

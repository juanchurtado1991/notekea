package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.core.presentation.keyDown
import app.notekea.core.presentation.keyUp
import app.notekea.core.presentation.nodes
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tab walks what has no shortcut of its own: the rail's bottom buttons, a section header, the segments and a bookmark
 * row, each showing it has the keyboard. The rail's daily buttons and the tree's rows stay out of the way.
 */
class KeyboardReachScreenTest {
    private var frame = 0L

    @Test
    fun `tab reaches the rail's bottom buttons, section headers, segments and bookmark rows`() {
        val reached = mutableListOf<String?>()

        UiThread.run {
            scene().use { scene ->
                settle(scene = scene)

                repeat(times = STOPS.size) {
                    scene.sendKeyEvent(event = keyDown(key = Key.Tab))
                    settle(scene = scene)

                    reached += scene
                        .nodes()
                        .firstOrNull { it.isFocused() }
                        ?.name()
                }
            }
        }

        assertEquals(
            expected = STOPS,
            actual = reached.map { it.orEmpty() }
        )
    }

    @Test
    fun `enter folds a header that has the keyboard`() {
        var folds = 0

        UiThread.run {
            ImageComposeScene(
                width = WIDTH,
                height = HEIGHT,
                density = Density(density = 1f)
            ) {
                NotekeaTheme(darkTheme = false) {
                    NotekeaSectionHeader(
                        title = HEADER,
                        onToggle = {
                            folds++
                        }
                    )
                }
            }.use { scene ->
                settle(scene = scene)
                scene.sendKeyEvent(event = keyDown(key = Key.Tab))
                settle(scene = scene)
                scene.sendKeyEvent(event = keyDown(key = Key.Enter))
                scene.sendKeyEvent(event = keyUp(key = Key.Enter))
                settle(scene = scene)
            }
        }

        assertEquals(
            expected = 1,
            actual = folds
        )
    }

    private fun scene() = ImageComposeScene(
        width = WIDTH,
        height = HEIGHT,
        density = Density(density = 1f)
    ) {
        NotekeaTheme(darkTheme = false) {
            Row {
                NotekeaToolRail(
                    items = listOf(
                        NotekeaToolRailItem(
                            icon = NotekeaIcons.Find,
                            label = DAILY,
                            onClick = {}
                        )
                    ),
                    label = RAIL,
                    bottomItems = listOf(
                        NotekeaToolRailItem(
                            icon = NotekeaIcons.Find,
                            label = BOTTOM,
                            onClick = {}
                        )
                    )
                )

                Column {
                    NotekeaSectionHeader(
                        title = HEADER,
                        onToggle = {}
                    )

                    NotekeaSegmentedControl(
                        segments = listOf(
                            NotekeaSegment(
                                icon = NotekeaIcons.Chat,
                                label = SEGMENT
                            )
                        ),
                        selectedIndex = 0,
                        onSelect = {}
                    )

                    NotekeaTreeItem(
                        label = BOOKMARK,
                        depth = 1,
                        onClick = {},
                        icon = null,
                        takesFocus = true
                    )

                    NotekeaTreeItem(
                        label = TREE_ROW,
                        depth = 1,
                        onClick = {},
                        icon = null
                    )
                }
            }
        }
    }

    private fun SemanticsNode.isFocused(): Boolean =
        config.getOrNull(SemanticsProperties.Focused) == true

    /** What the node says, from itself or the text it holds. */
    private fun SemanticsNode.name(): String? {
        val own = config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()
            ?: config
                .getOrNull(SemanticsProperties.Text)
                ?.firstOrNull()
                ?.text

        return own ?: children.firstNotNullOfOrNull { it.name() }
    }

    private fun settle(scene: ImageComposeScene) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val BOOKMARK = "Roadmap"
        private const val BOTTOM = "Settings"
        private const val DAILY = "Search"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEADER = "Bookmarks"
        private const val HEIGHT = 400
        private const val RAIL = "Tools"
        private const val SEGMENT = "Chat"
        private const val SETTLE_FRAMES = 4
        private const val TREE_ROW = "Tree row"
        private const val WIDTH = 500
        private val STOPS = listOf(BOTTOM, HEADER, SEGMENT, BOOKMARK)
    }
}

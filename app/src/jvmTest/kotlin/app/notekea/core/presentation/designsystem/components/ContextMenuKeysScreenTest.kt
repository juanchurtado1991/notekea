package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.core.presentation.keyDown
import app.notekea.core.presentation.keyUp
import app.notekea.core.presentation.nodes
import app.notekea.core.presentation.texts
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A menu works from the keyboard: Shift+F10 and the Menu key open it on what has the keyboard, a tab or a row among
 * them; Right goes into a submenu, its rows take Enter, and Left comes back.
 */
class ContextMenuKeysScreenTest {
    private var frame = 0L

    @Test
    fun `the Menu key and Shift+F10 open the menu of what has the keyboard`() {
        val shown = listOf(
            keyDown(key = Key.Menu),
            keyDown(
                key = Key.F10,
                isShiftPressed = true
            )
        ).map { key ->
            var isShown = false

            UiThread.run {
                menuScene().use { scene ->
                    settle(scene = scene)
                    scene.sendKeyEvent(event = keyDown(key = Key.Tab))
                    settle(scene = scene)
                    scene.sendKeyEvent(event = key)
                    settle(scene = scene)
                    isShown = PARENT in scene.texts()
                }
            }

            isShown
        }

        assertEquals(
            expected = listOf(true, true),
            actual = shown
        )
    }

    @Test
    fun `the Menu key opens the selected tab's menu, which takes the keyboard`() {
        var isShown = false
        var chosen = 0

        UiThread.run {
            ImageComposeScene(
                width = WIDTH,
                height = HEIGHT,
                density = Density(density = 1f)
            ) {
                NotekeaTheme(darkTheme = false) {
                    NotekeaTabStrip(
                        tabs = listOf(
                            NotekeaTabStripItem(
                                key = TAB,
                                label = TAB
                            )
                        ),
                        selectedIndex = 0,
                        isActive = true,
                        closeLabel = CHILD,
                        onSelect = {},
                        onClose = {},
                        onDrop = { _, _ -> },
                        onReorder = { _, _ -> },
                        menuOf = {
                            listOf(
                                listOf(
                                    NotekeaMenuItem(
                                        label = PARENT,
                                        onClick = {
                                            chosen++
                                        }
                                    )
                                )
                            )
                        }
                    )
                }
            }.use { scene ->
                settle(scene = scene)
                scene.sendKeyEvent(event = keyDown(key = Key.Tab))
                settle(scene = scene)
                scene.sendKeyEvent(event = keyDown(key = Key.Menu))
                settle(scene = scene)
                isShown = PARENT in scene.texts()

                press(
                    scene = scene,
                    key = Key.DirectionDown
                )

                press(
                    scene = scene,
                    key = Key.Enter
                )
            }
        }

        assertEquals(
            expected = true to 1,
            actual = isShown to chosen
        )
    }

    @Test
    fun `Right goes into a submenu, Left comes back, and Enter chooses in it`() {
        var chosen = 0
        var inside = false
        var afterLeft = true

        UiThread.run {
            menuScene(
                isOpen = true,
                onChild = {
                    chosen++
                }
            ).use { scene ->
                settle(scene = scene)

                press(
                    scene = scene,
                    key = Key.DirectionDown
                )

                press(
                    scene = scene,
                    key = Key.DirectionRight
                )

                inside = CHILD in scene.texts()

                press(
                    scene = scene,
                    key = Key.DirectionLeft
                )

                afterLeft = CHILD in scene.texts()

                press(
                    scene = scene,
                    key = Key.DirectionRight
                )

                press(
                    scene = scene,
                    key = Key.Enter
                )
            }
        }

        assertEquals(
            expected = Triple(true, false, 1),
            actual = Triple(inside, afterLeft, chosen)
        )
    }

    @Test
    fun `a submenu opened with the pointer still closes when the pointer moves on`() {
        var opened = false
        var moved = true

        UiThread.run {
            menuScene(
                isOpen = true,
                onChild = {}
            ).use { scene ->
                settle(scene = scene)
                val rows = scene.textCenters()

                scene.sendPointerEvent(
                    eventType = PointerEventType.Move,
                    position = rows.getValue(PARENT)
                )

                settle(scene = scene)
                opened = CHILD in scene.texts()

                scene.sendPointerEvent(
                    eventType = PointerEventType.Move,
                    position = rows.getValue(SIBLING)
                )

                settle(scene = scene)
                moved = CHILD in scene.texts()
            }
        }

        assertEquals(
            expected = true to false,
            actual = opened to moved
        )
    }

    private fun ImageComposeScene.textCenters(): Map<String, Offset> = listOf(
        PARENT,
        SIBLING
    ).associateWith { text ->
        nodes()
            .first { node ->
                node
                    .config
                    .getOrNull(SemanticsProperties.Text)
                    ?.any {
                        it.text == text
                    } == true
            }
            .boundsInRoot
            .center
    }

    private fun menuScene(
        isOpen: Boolean = false,
        onChild: () -> Unit = {}
    ) = ImageComposeScene(
        width = WIDTH,
        height = HEIGHT,
        density = Density(density = 1f)
    ) {
        NotekeaTheme(darkTheme = false) {
            val state = remember {
                NotekeaContextMenuState().apply {
                    if (isOpen) open(byKeyboard = true)
                }
            }

            NotekeaContextMenuArea(
                sections = listOf(
                    listOf(
                        NotekeaMenuItem(
                            label = PARENT,
                            onClick = {},
                            items = listOf(
                                listOf(
                                    NotekeaMenuItem(
                                        label = CHILD,
                                        onClick = onChild
                                    )
                                )
                            )
                        ),
                        NotekeaMenuItem(
                            label = SIBLING,
                            onClick = {}
                        )
                    )
                ),
                state = state
            ) { Box(modifier = Modifier.size(size = ANCHOR_DP.dp).focusable()) }
        }
    }

    private fun press(
        scene: ImageComposeScene,
        key: Key
    ) {
        scene.sendKeyEvent(event = keyDown(key = key))
        scene.sendKeyEvent(event = keyUp(key = key))
        settle(scene = scene)
    }

    private fun settle(scene: ImageComposeScene) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val ANCHOR_DP = 40
        private const val CHILD = "Plain text"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 500
        private const val PARENT = "Code block"
        private const val SETTLE_FRAMES = 6
        private const val SIBLING = "Quote"
        private const val TAB = "Arepas"
        private const val WIDTH = 700
    }
}

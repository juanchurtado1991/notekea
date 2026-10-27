package app.notekea.notes.presentation.tree

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** A row's menu in the tree offers "New note from template" (`ROADMAP.md` 1.12), between New note and New folder. */
class TreeTemplateMenuTest {
    @Test
    fun `new note from template in a row's menu asks for a template`() {
        val intents = mutableListOf<VaultTreeIntent>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    VaultTreeContent(
                        state = TreePreviewData.state(),
                        vaultName = TreePreviewData.VAULT_NAME,
                        onIntent = { intents += it },
                        onSwitchVault = {},
                        onSearch = {}
                    ) {}
                }
            }.use { scene ->
                var frame = 0L
                val settle = { repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) } }
                settle()
                scene.sendPointerEvent(eventType = PointerEventType.Move, position = ROW)
                settle()
                click(scene = scene, position = ROW, button = PointerButton.Secondary, settle = settle)
                scene.sendPointerEvent(eventType = PointerEventType.Move, position = MENU_ITEM)
                click(scene = scene, position = MENU_ITEM, button = PointerButton.Primary, settle = settle)
            }
        }

        assertEquals(
            expected = listOf<VaultTreeIntent>(VaultTreeIntent.OnNewNoteFromTemplateClick),
            actual = intents.filterIsInstance<VaultTreeIntent.OnNewNoteFromTemplateClick>()
        )
    }

    /** Presses and releases [button] at [position], letting the menu open or close between the two. */
    private fun click(
        scene: ImageComposeScene,
        position: Offset,
        button: PointerButton,
        settle: () -> Unit
    ) {
        listOf(PointerEventType.Press, PointerEventType.Release).forEach { type ->
            val isPressed = type == PointerEventType.Press
            scene.sendPointerEvent(
                eventType = type,
                position = position,
                buttons = PointerButtons(
                    isPrimaryPressed = isPressed && button == PointerButton.Primary,
                    isSecondaryPressed = isPressed && button == PointerButton.Secondary
                ),
                button = button
            )
            settle()
        }
    }

    companion object {
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 420
        private const val SETTLE_FRAMES = 10
        private const val WIDTH = 1000
        private val MENU_ITEM = Offset(x = 260f, y = 121f)
        private val ROW = Offset(x = 150f, y = 75f)
    }
}

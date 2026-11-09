package app.notekea.core.presentation.settings

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.appearance.domain.ThemeMode
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.clickText
import app.notekea.core.presentation.nodes
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Settings → Appearance (`ROADMAP.md` 2.12), rendered headless: a theme is chosen with a click and another with the
 * keyboard (its card focused, then Enter), and the mode with its segment; each choice is kept.
 */
@OptIn(InternalComposeUiApi::class)
class AppearanceSettingsScreenTest {
    private var frame = 0L

    @Test
    fun `a theme is chosen with the mouse or the keyboard, and the mode with its segment`() {
        val store = FakeAppearanceStore()
        val kept = mutableListOf<Appearance>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    SettingsDialog(
                        onDismiss = {},
                        aiModelSection = {},
                        viewModel = AppearanceViewModel(appearanceStore = store)
                    )
                }
            }.use { scene ->
                settle(scene = scene)
                scene.clickText(text = "Sage") { settle(scene = scene) }
                kept += store.kept
                focusCard(scene = scene, name = "Honey")
                scene.sendKeyEvent(event = KeyEvent(key = Key.Enter, type = KeyEventType.KeyDown))
                scene.sendKeyEvent(event = KeyEvent(key = Key.Enter, type = KeyEventType.KeyUp))
                settle(scene = scene)
                kept += store.kept
                scene.clickText(text = "Dark") { settle(scene = scene) }
                kept += store.kept
            }
        }

        assertEquals(
            expected = listOf(
                Appearance(theme = ColorTheme.SAGE),
                Appearance(theme = ColorTheme.HONEY),
                Appearance(theme = ColorTheme.HONEY, mode = ThemeMode.DARK)
            ),
            actual = kept
        )
    }

    /** Gives the keyboard to the card of the theme [name], as Tab would. */
    private fun focusCard(
        scene: ImageComposeScene,
        name: String
    ) {
        val card = scene.nodes().first { node ->
            node.config.getOrNull(SemanticsProperties.Role) == Role.RadioButton && name in textsUnder(node = node)
        }
        card.config[SemanticsActions.RequestFocus].action?.invoke()
        settle(scene = scene)
    }

    private fun textsUnder(
        node: SemanticsNode
    ): List<String> = node.children.flatMap { child ->
        child.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text } + textsUnder(node = child)
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 700
        private const val SETTLE_FRAMES = 5
        private const val WIDTH = 1000
    }
}

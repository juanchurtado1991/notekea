package app.notekea.vault.presentation.welcome

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.vault.domain.Vault
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The welcome screen's recent vaults (`ROADMAP.md` 1.14), rendered headless: five show, the sixth is not on screen
 * until the list is scrolled inside its card, where it can be opened.
 */
class WelcomeRecentsScreenTest {
    private var frame = 0L

    @Test
    fun `five recent vaults show and the list scrolls inside its card to the others`() {
        val intents = mutableListOf<WelcomeIntent>()
        val vaults = (1..RECENTS).map { index ->
            WelcomePreviewData.recentVaults.first().let {
                it.copy(vault = Vault(path = PATH + index, name = NAME + index))
            }
        }
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    WelcomeContent(
                        state = WelcomePreviewData.state().copy(recentVaults = vaults),
                        onIntent = { intents += it },
                        openedVaultContent = { _, _ -> }
                    )
                }
            }.use { scene ->
                settle(scene = scene)
                listOf(FIFTH_ROW, BELOW_CARD).forEach { click(scene = scene, position = it) }
                scene.sendPointerEvent(
                    eventType = PointerEventType.Scroll,
                    position = FIFTH_ROW,
                    scrollDelta = Offset(x = 0f, y = SCROLL_TICKS)
                )
                settle(scene = scene)
                click(scene = scene, position = FIFTH_ROW)
            }
        }

        assertEquals(
            expected = listOf(PATH + 5, PATH + RECENTS),
            actual = intents.filterIsInstance<WelcomeIntent.OnRecentVaultClick>().map { it.path }
        )
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

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 760
        private const val NAME = "Vault "
        private const val PATH = "/home/user/Vault"
        private const val RECENTS = 7

        // Far more than the list holds: a wheel tick scrolls a different distance on each system (CI runs all three).
        private const val SCROLL_TICKS = 100f
        private const val SETTLE_FRAMES = 4
        private const val WIDTH = 1300
        private val BELOW_CARD = Offset(x = 900f, y = 560f)
        private val FIFTH_ROW = Offset(x = 900f, y = 495f)
    }
}

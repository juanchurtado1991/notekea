package app.notekea.vault.presentation.welcome

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.texts
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.vault.domain.VaultError
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Smoke 1.3-09 on macOS: Open folder… from an open vault on a folder Notekea cannot read did nothing, because the
 * error went to the welcome screen behind the vault. It now shows over the open vault.
 */
class WelcomeOpenErrorScreenTest {
    @Test
    fun `a folder that cannot be read from the open vault shows its error over it`() {
        var texts = emptyList<String>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    WelcomeContent(
                        state = WelcomePreviewData.state().copy(
                            openedVault = WelcomePreviewData.openedVault,
                            error = VaultError.PermissionDenied(path = LOCKED_PATH)
                        ),
                        onIntent = {},
                        openedVaultContent = { _, _ -> }
                    )
                }
            }.use { scene ->
                repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) }
                texts = scene.texts()
            }
        }

        assertTrue(actual = texts.any { LOCKED_NAME in it && "permission" in it }, message = texts.toString())
    }

    companion object {
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 640
        private const val LOCKED_NAME = "Locked Vault"
        private const val LOCKED_PATH = "/home/ana/$LOCKED_NAME"
        private const val SETTLE_FRAMES = 5
        private const val WIDTH = 1000
    }
}

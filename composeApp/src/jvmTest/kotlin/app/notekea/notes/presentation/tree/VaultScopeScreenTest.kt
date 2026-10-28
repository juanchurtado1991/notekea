package app.notekea.notes.presentation.tree

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.use
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.platform.BeforeWindowClose
import app.notekea.core.presentation.platform.LocalWindowCloseGuard
import app.notekea.core.presentation.platform.WindowCloseGuard
import androidx.compose.runtime.CompositionLocalProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A vault's scope (`ROADMAP.md` 1.18), rendered headless: leaving the vault first lets its screens save, and once the
 * vault is gone its ViewModels are cleared; the window closing also waits for the vault's screens.
 */
class VaultScopeScreenTest {
    private val events = mutableListOf<String>()

    @Test
    fun `leaving a vault saves first, then clears its view models once it is gone`() {
        var isOpen by mutableStateOf(value = true)
        var close: ((() -> Unit) -> Unit)? = null
        UiThread.run {
            ImageComposeScene(width = SIZE, height = SIZE) {
                if (isOpen) {
                    VaultScope(vaultPath = VAULT) { onClose ->
                        close = onClose
                        viewModel { RecordingViewModel(events = events) }
                        BeforeWindowClose { events += SAVED }
                    }
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                close?.invoke { events += LEFT }
                scene.render(nanoTime = 1L)
                isOpen = false
                scene.render(nanoTime = 2L)
            }
        }

        assertEquals(expected = listOf(SAVED, LEFT, CLEARED), actual = events)
    }

    @Test
    fun `closing the window waits for the open vault's screens`() {
        val window = WindowCloseGuard()
        UiThread.run {
            ImageComposeScene(width = SIZE, height = SIZE) {
                CompositionLocalProvider(LocalWindowCloseGuard provides window) {
                    VaultScope(vaultPath = VAULT) { BeforeWindowClose { events += SAVED } }
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                runBlocking { window.beforeClose() }
            }
        }

        assertEquals(expected = listOf(SAVED), actual = events)
    }

    private class RecordingViewModel(
        private val events: MutableList<String>
    ) : ViewModel() {
        override fun onCleared() {
            events += CLEARED
        }
    }

    companion object {
        private const val CLEARED = "cleared"
        private const val LEFT = "left"
        private const val SAVED = "saved"
        private const val SIZE = 100
        private const val VAULT = "/vaults/Personal Notes"
    }
}

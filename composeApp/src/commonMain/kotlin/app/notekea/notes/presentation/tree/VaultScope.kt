package app.notekea.notes.presentation.tree

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import app.notekea.core.presentation.platform.BeforeWindowClose
import app.notekea.core.presentation.platform.LocalWindowCloseGuard
import app.notekea.core.presentation.platform.WindowCloseGuard
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import kotlinx.coroutines.launch

private const val PREVIEW_TEXT = "A vault's workspace"

/**
 * The life of the vault at [vaultPath] while it is open (`ROADMAP.md` 1.18). Its ViewModels live in a store of their
 * own, cleared when the vault closes, so the welcome screen or the next vault never keeps the old one's tree, watcher,
 * caches and editors in memory. The screens inside save what they hold with [BeforeWindowClose], as for the window:
 * their handlers run when the window closes, and before the vault closes through the function [content] gets, which
 * runs its argument (leaving the vault) once they are done.
 */
@Composable
fun VaultScope(
    vaultPath: String,
    content: @Composable (close: (then: () -> Unit) -> Unit) -> Unit
) {
    val store = remember(vaultPath) { ViewModelStore() }
    val owner = remember(store) {
        object : ViewModelStoreOwner {
            override val viewModelStore = store
        }
    }
    val vaultGuard = remember(vaultPath) { WindowCloseGuard() }
    val scope = rememberCoroutineScope()
    val close = remember(vaultGuard) {
        { then: () -> Unit ->
            scope.launch {
                vaultGuard.beforeClose()
                then()
            }
            Unit
        }
    }
    BeforeWindowClose { vaultGuard.beforeClose() }
    DisposableEffect(store) { onDispose { store.clear() } }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides owner,
        LocalWindowCloseGuard provides vaultGuard
    ) { content(close) }
}

@Preview
@Composable
private fun VaultScopePreview() {
    NotekeaPreviewThemes {
        VaultScope(vaultPath = PREVIEW_TEXT) { Text(text = PREVIEW_TEXT) }
    }
}

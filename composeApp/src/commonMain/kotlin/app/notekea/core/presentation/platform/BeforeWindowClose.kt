package app.notekea.core.presentation.platform

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes

private const val PREVIEW_TEXT = "Saves before the window closes while this is shown"

/**
 * Registers [action] with the window's [WindowCloseGuard] while this is in the composition, so closing the window waits
 * for it; [action] always runs its latest version. It draws nothing.
 */
@Composable
fun BeforeWindowClose(
    action: suspend () -> Unit
) {
    val guard = LocalWindowCloseGuard.current
    val currentAction by rememberUpdatedState(newValue = action)
    DisposableEffect(guard) {
        val unregister = guard.register { currentAction() }
        onDispose { unregister() }
    }
}

@Preview
@Composable
private fun BeforeWindowClosePreview() {
    NotekeaPreviewThemes {
        BeforeWindowClose(action = {})
        Text(text = PREVIEW_TEXT)
    }
}

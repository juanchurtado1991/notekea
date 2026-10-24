package app.notekea.core.presentation.platform

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.platform.domain.ShortcutKey
import app.notekea.core.platform.domain.ShortcutModifier
import app.notekea.core.presentation.preview.NotekeaPreviewThemes

private const val PREVIEW_TEXT = "Ctrl+O is handled while this is shown"

/**
 * Registers [shortcut] with the window's [ShortcutDispatcher] while this is in the composition, so it works whatever
 * has focus; [onTrigger] always runs its latest version. It draws nothing.
 */
@Composable
fun WindowShortcut(
    shortcut: Shortcut,
    onTrigger: () -> Unit
) {
    val dispatcher = LocalShortcutDispatcher.current
    val currentOnTrigger by rememberUpdatedState(newValue = onTrigger)
    DisposableEffect(dispatcher, shortcut) {
        val unregister = dispatcher.register { event ->
            val isMatch = shortcut.matches(event = event)
            if (isMatch) currentOnTrigger()
            isMatch
        }
        onDispose { unregister() }
    }
}

@Preview
@Composable
private fun WindowShortcutPreview() {
    NotekeaPreviewThemes {
        WindowShortcut(
            shortcut = Shortcut(key = ShortcutKey.O, modifiers = setOf(ShortcutModifier.CONTROL)),
            onTrigger = {}
        )
        Text(text = PREVIEW_TEXT)
    }
}

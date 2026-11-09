package app.notekea.core.presentation.platform

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions

private const val PREVIEW_TEXT = "Close window and Quit are handled while this is shown"

/**
 * The system's quit shortcut and Close window (`PLATFORM_GUIDELINES.md` §3). Notekea has one window, so both end up in
 * [onClose], the same path as the window's close button, which lets the [WindowCloseGuard] finish. It draws nothing.
 */
@Composable
fun WindowCloseShortcuts(
    platformConventions: PlatformConventions,
    onClose: () -> Unit
) {
    WindowShortcut(
        shortcut = platformConventions.quitShortcut,
        onTrigger = onClose
    )

    WindowShortcut(
        shortcut = platformConventions.closeWindowShortcut,
        onTrigger = onClose
    )
}

@Preview
@Composable
private fun WindowCloseShortcutsPreview() {
    NotekeaPreviewThemes {
        WindowCloseShortcuts(
            platformConventions = PreviewPlatformConventions.linux,
            onClose = {}
        )

        Text(text = PREVIEW_TEXT)
    }
}

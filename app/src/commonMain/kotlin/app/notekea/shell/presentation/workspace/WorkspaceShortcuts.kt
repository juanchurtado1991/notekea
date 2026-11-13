package app.notekea.shell.presentation.workspace

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.platform.WindowShortcut

/**
 * The tabs' shortcuts, whatever has focus: `Mod+W` closes the active tab, `Ctrl+Tab` and `Ctrl+Shift+Tab` cycle through
 * the active pane's tabs on every system, `Mod+1` to `Mod+9` jump to one and `Mod+Shift+D` splits right; plus the side
 * panel's: `Mod+Shift+\` toggles it ([onSidePanelToggle]) and `Mod+Shift+O` goes to the outline.
 */
@Composable
fun WorkspaceShortcuts(
    platformConventions: PlatformConventions,
    onIntent: (WorkspaceIntent) -> Unit,
    onSidePanelToggle: () -> Unit
) {
    WindowShortcut(shortcut = platformConventions.closeTabShortcut) {
        onIntent(WorkspaceIntent.OnCloseActive)
    }

    WindowShortcut(shortcut = platformConventions.nextTabShortcut) {
        onIntent(WorkspaceIntent.OnCycle(step = 1))
    }

    WindowShortcut(shortcut = platformConventions.previousTabShortcut) {
        onIntent(WorkspaceIntent.OnCycle(step = -1))
    }

    WindowShortcut(shortcut = platformConventions.splitRightShortcut) {
        onIntent(WorkspaceIntent.OnSplitRight)
    }

    platformConventions.goToTabShortcuts.forEachIndexed { index, shortcut ->
        WindowShortcut(shortcut = shortcut) { onIntent(WorkspaceIntent.OnJump(number = index + 1)) }
    }

    WindowShortcut(shortcut = platformConventions.toggleSidePanelShortcut) { onSidePanelToggle() }

    WindowShortcut(shortcut = platformConventions.outlineShortcut) {
        onIntent(WorkspaceIntent.OnOutlineFocusRequest)
    }

    WindowShortcut(shortcut = platformConventions.chatShortcut) {
        onIntent(WorkspaceIntent.OnChatFocusRequest)
    }
}

@Preview
@Composable
private fun WorkspaceShortcutsPreview() {
    NotekeaPreviewThemes {
        WorkspaceShortcuts(
            platformConventions = PreviewPlatformConventions.linux,
            onIntent = {
            },
            onSidePanelToggle = {}
        )
    }
}

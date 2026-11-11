package app.notekea.shell.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.settingsShortcut
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.core.presentation.platform.WindowDialogs
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.settings.presentation.SettingsDialog
import app.notekea.settings.presentation.SettingsSection
import app.notekea.settings.presentation.about.AboutDialog

private const val PREVIEW_TEXT = "About or Settings open over this"

/**
 * About or Settings over the window while [dialogs] says so, with a vault open or on the welcome screen; Settings
 * shows [aiModelSection] and [syncSection] as its AI model and Sync sections.
 */
@Composable
fun WindowDialogsHost(
    platformConventions: PlatformConventions,
    dialogs: WindowDialogs,
    aiModelSection: @Composable (Modifier, SettingsSpotlight?) -> Unit,
    syncSection: @Composable (Modifier, SettingsSpotlight?) -> Unit,
    vaultPath: String?
) {
    val onDismiss = { dialogs.shown = null }

    // Mod+, opens Settings from anywhere, the welcome screen too (5.2).
    WindowShortcut(shortcut = platformConventions.settingsShortcut) {
        dialogs.shown = WindowDialog.SETTINGS
    }

    when (dialogs.shown) {
        WindowDialog.ABOUT -> AboutDialog(
            platformConventions = platformConventions,
            onDismiss = onDismiss
        )

        WindowDialog.SETTINGS, WindowDialog.AI_MODEL, WindowDialog.SYNC -> SettingsDialog(
            onDismiss = onDismiss,
            platformConventions = platformConventions,
            vaultPath = vaultPath,
            aiModelSection = aiModelSection,
            syncSection = syncSection,
            initialSection = sectionOf(dialog = dialogs.shown)
        )

        null -> Unit
    }
}

/** The section Settings opens at for [dialog]: AI model or Sync when asked for, General otherwise. */
private fun sectionOf(dialog: WindowDialog?): SettingsSection = when (dialog) {
    WindowDialog.AI_MODEL -> SettingsSection.AI_MODEL
    WindowDialog.SYNC -> SettingsSection.SYNC
    else -> SettingsSection.GENERAL
}

@Preview
@Composable
private fun WindowDialogsHostPreview() {
    NotekeaPreviewThemes {
        Text(text = PREVIEW_TEXT)

        WindowDialogsHost(
            platformConventions = PreviewPlatformConventions.linux,
            dialogs = WindowDialogs(),
            vaultPath = null,
            aiModelSection = { _, _ -> },
            syncSection = { _, _ -> }
        )
    }
}

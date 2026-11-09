package app.notekea.shell.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.core.presentation.platform.WindowDialogs
import app.notekea.settings.presentation.SettingsDialog
import app.notekea.settings.presentation.about.AboutDialog

private const val PREVIEW_TEXT = "About or Settings open over this"

/**
 * About or Settings over the window while [dialogs] says so, with a vault open or on the welcome screen; Settings
 * shows [aiModelSection] as its AI model section.
 */
@Composable
fun WindowDialogsHost(
    platformConventions: PlatformConventions,
    dialogs: WindowDialogs,
    aiModelSection: @Composable (Modifier) -> Unit
) {
    val onDismiss = { dialogs.shown = null }

    when (dialogs.shown) {
        WindowDialog.ABOUT -> AboutDialog(
            platformConventions = platformConventions,
            onDismiss = onDismiss
        )

        WindowDialog.SETTINGS -> SettingsDialog(
            onDismiss = onDismiss,
            aiModelSection = aiModelSection
        )

        null -> Unit
    }
}

@Preview
@Composable
private fun WindowDialogsHostPreview() {
    NotekeaPreviewThemes {
        Text(text = PREVIEW_TEXT)

        WindowDialogsHost(
            platformConventions = PreviewPlatformConventions.linux,
            dialogs = WindowDialogs(),
            aiModelSection = {}
        )
    }
}

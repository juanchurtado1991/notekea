package app.notekea.core.presentation.platform

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.about.AboutDialog
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.settings.SettingsDialog

private const val PREVIEW_TEXT = "About or Settings open over this"

/** About or Settings over the window while [dialogs] says so, with a vault open or on the welcome screen. */
@Composable
fun WindowDialogsHost(
    platformConventions: PlatformConventions,
    dialogs: WindowDialogs
) {
    val onDismiss = { dialogs.shown = null }
    when (dialogs.shown) {
        WindowDialog.ABOUT -> AboutDialog(platformConventions = platformConventions, onDismiss = onDismiss)
        WindowDialog.SETTINGS -> SettingsDialog(onDismiss = onDismiss)
        null -> Unit
    }
}

@Preview
@Composable
private fun WindowDialogsHostPreview() {
    NotekeaPreviewThemes {
        Text(text = PREVIEW_TEXT)
        WindowDialogsHost(platformConventions = PreviewPlatformConventions.linux, dialogs = WindowDialogs())
    }
}

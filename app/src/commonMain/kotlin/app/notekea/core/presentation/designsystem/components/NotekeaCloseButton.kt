package app.notekea.core.presentation.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val PREVIEW_LABEL = "Close"

/**
 * The X that closes a dialog (Settings, About), in the theme's red so it is found at a glance (the developer's
 * request of 2026-11-14: in the text's gray it was lost in the dialog's corner). Esc closes too, so it takes no focus.
 */
@Composable
fun NotekeaCloseButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NotekeaIconButton(
        icon = NotekeaIcons.Close,
        label = label,
        onClick = onClick,
        modifier = modifier,
        takesFocus = false,
        tint = MaterialTheme.colorScheme.error
    )
}

@Preview
@Composable
private fun NotekeaCloseButtonPreview() {
    NotekeaPreviewThemes {
        NotekeaCloseButton(
            label = PREVIEW_LABEL,
            onClick = {}
        )
    }
}

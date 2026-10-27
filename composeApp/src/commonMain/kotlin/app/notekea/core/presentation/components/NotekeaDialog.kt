package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val ELEVATION_DP = 12
private const val PREVIEW_HEIGHT_DP = 200
private const val PREVIEW_TEXT = "About Notekea"
private const val PREVIEW_WIDTH_DP = 360

/**
 * A modal panel over the window (About, Settings): the window dims behind it, and Esc or a click outside closes it
 * ([onDismiss]). [content] lays itself out, at the size [modifier] gives it.
 */
@Composable
fun NotekeaDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        NotekeaDialogSurface(modifier = modifier, content = content)
    }
}

/** The dialog's card: the large shape, the surface color, a hairline and a shadow. */
@Composable
private fun NotekeaDialogSurface(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = MaterialTheme.colorScheme.outline),
        shadowElevation = ELEVATION_DP.dp,
        content = content
    )
}

@Preview
@Composable
private fun NotekeaDialogPreview() {
    NotekeaPreviewThemes {
        NotekeaDialogSurface(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            Box(modifier = Modifier.padding(all = NotekeaDimensions.SpacingXl)) { Text(text = PREVIEW_TEXT) }
        }
    }
}

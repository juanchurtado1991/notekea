package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val PREVIEW_WIDTH_DP = 420
private const val PREVIEW_TITLE = "Files too large to sync"
private const val PREVIEW_EXPLANATION =
    "These files stay on this computer and are no longer synced."
private const val PREVIEW_FILE = "Videos/Lisbon trip.mp4"

/**
 * A [NotekeaDialog] that says or asks something: its [title], an [explanation] under it in the muted color, and
 * [content] below (fields, choices, the buttons that end it), spaced as every such dialog is.
 */
@Composable
fun NotekeaFormDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    explanation: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    NotekeaDialog(
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingL),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            explanation?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            content()
        }
    }
}

@Preview
@Composable
private fun NotekeaFormDialogPreview() {
    NotekeaPreviewThemes {
        NotekeaFormDialog(
            title = PREVIEW_TITLE,
            onDismiss = {},
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            explanation = PREVIEW_EXPLANATION
        ) {
            Text(
                text = PREVIEW_FILE,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

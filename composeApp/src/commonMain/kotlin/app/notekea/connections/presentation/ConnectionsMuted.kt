package app.notekea.connections.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

/** A line of Connections that says why a tab lists nothing, or what went wrong, in the muted color. */
@Composable
fun ConnectionsMuted(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingS),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun ConnectionsMutedPreview() {
    NotekeaPreviewThemes { ConnectionsMuted(text = "No note links here yet.") }
}

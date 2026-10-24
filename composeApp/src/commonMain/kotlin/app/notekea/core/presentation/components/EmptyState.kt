package app.notekea.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.notekea.core.presentation.theme.NotekeaDimensions
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes

private const val PREVIEW_ACTION = "New note"
private const val PREVIEW_MESSAGE = "Create your first note to get started."
private const val PREVIEW_TITLE = "This folder has no notes yet"

/** Centered empty state with a title, an explanation and an optional action slot (usually a [NotekeaButton]). */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(all = NotekeaDimensions.SpacingXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        action?.let {
            Column(modifier = Modifier.padding(top = NotekeaDimensions.SpacingS)) { it() }
        }
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    NotekeaPreviewThemes {
        EmptyState(title = PREVIEW_TITLE, message = PREVIEW_MESSAGE, action = {
            NotekeaButton(text = PREVIEW_ACTION, onClick = {})
        })
    }
}

@Preview
@Composable
private fun EmptyStateWithoutActionPreview() {
    NotekeaPreviewThemes {
        EmptyState(title = PREVIEW_TITLE, message = PREVIEW_MESSAGE)
    }
}

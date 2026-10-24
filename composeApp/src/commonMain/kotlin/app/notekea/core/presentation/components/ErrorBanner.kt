package app.notekea.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.error_banner_dismiss
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp

private const val PREVIEW_ACTION = "Load theirs"
private const val PREVIEW_MESSAGE = "This note changed outside Notekea."
private const val PREVIEW_WIDTH_DP = 480

/**
 * Inline error that keeps the content around it visible: a message, an optional action slot (e.g. "Retry",
 * "Locate…") and an optional dismiss button.
 */
@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(width = NotekeaDimensions.BorderWidth, color = colors.error, shape = MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small,
        color = colors.surface,
        contentColor = colors.error
    ) {
        Row(
            modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = message, modifier = Modifier.weight(weight = 1f), style = MaterialTheme.typography.bodyMedium)
            action?.invoke()
            onDismiss?.let {
                TextButton(onClick = it) { Text(text = stringResource(resource = Res.string.error_banner_dismiss)) }
            }
        }
    }
}

@Preview
@Composable
private fun ErrorBannerPreview() {
    NotekeaPreviewThemes {
        Column(
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
        ) {
            ErrorBanner(
                message = PREVIEW_MESSAGE,
                onDismiss = {},
                action = { NotekeaButton(text = PREVIEW_ACTION, onClick = {}, style = NotekeaButtonStyle.SECONDARY) }
            )
            ErrorBanner(message = PREVIEW_MESSAGE)
        }
    }
}

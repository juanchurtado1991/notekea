package app.notekea.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
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
private const val PREVIEW_NARROW_WIDTH_DP = 240
private const val PREVIEW_WIDTH_DP = 480
private const val STACKED_BELOW_DP = 360

/**
 * Inline error that keeps the content around it visible: a message, an optional action slot (e.g. "Retry",
 * "Locate…") and an optional dismiss button. Narrower than [STACKED_BELOW_DP] (a sidebar), the buttons move below the
 * message, so the message keeps the full width instead of wrapping into a thin column.
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
        BoxWithConstraints(
            modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingS)
        ) {
            val buttons: @Composable () -> Unit = { BannerButtons(onDismiss = onDismiss, action = action) }
            if (maxWidth < STACKED_BELOW_DP.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
                    BannerMessage(message = message, modifier = Modifier)
                    if (action != null ||
                        onDismiss != null
                    ) {
                        Row(modifier = Modifier.align(alignment = Alignment.End)) { buttons() }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BannerMessage(message = message, modifier = Modifier.weight(weight = 1f))
                    buttons()
                }
            }
        }
    }
}

@Composable
private fun BannerMessage(
    message: String,
    modifier: Modifier
) {
    Text(text = message, modifier = modifier, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun BannerButtons(
    onDismiss: (() -> Unit)?,
    action: (@Composable () -> Unit)?
) {
    action?.invoke()
    onDismiss?.let {
        NotekeaButton(
            text = stringResource(resource = Res.string.error_banner_dismiss),
            onClick = it,
            style = NotekeaButtonStyle.TEXT
        )
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

@Preview
@Composable
private fun ErrorBannerNarrowPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_NARROW_WIDTH_DP.dp)) {
            ErrorBanner(
                message = PREVIEW_MESSAGE,
                onDismiss = {},
                action = { NotekeaButton(text = PREVIEW_ACTION, onClick = {}, style = NotekeaButtonStyle.SECONDARY) }
            )
        }
    }
}

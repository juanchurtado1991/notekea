package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val PREVIEW_MESSAGE = "Notekea is reading your notes. You can keep writing."
private const val PREVIEW_DISMISS = "Dismiss"

/**
 * A notice that tells something good to know, not an error ([ErrorBanner] is for those): its [icon] and [message] on
 * the primary container color, and a button that hides it ([dismissLabel], [onDismiss]). Screen readers read it when
 * it appears.
 */
@Composable
fun NotekeaNotice(
    message: String,
    icon: ImageVector,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .background(color = colors.primaryContainer)
            .padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingS)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = colors.primary
        )
        Text(
            text = message,
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onPrimaryContainer
        )
        NotekeaButton(text = dismissLabel, onClick = onDismiss, style = NotekeaButtonStyle.SECONDARY)
    }
}

@Preview
@Composable
private fun NotekeaNoticePreview() {
    NotekeaPreviewThemes {
        NotekeaNotice(
            message = PREVIEW_MESSAGE,
            icon = NotekeaIcons.FeatureAi,
            dismissLabel = PREVIEW_DISMISS,
            onDismiss = {}
        )
    }
}

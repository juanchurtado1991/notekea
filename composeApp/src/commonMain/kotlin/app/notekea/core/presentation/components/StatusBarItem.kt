package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import app.notekea.core.presentation.theme.NotekeaDimensions
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes

private const val PREVIEW_INDEXED = "Indexed 1,204 notes"
private const val PREVIEW_MODEL = "Model: ready"
private const val PREVIEW_SYNC = "Sync: conflict"

/**
 * Clickable status bar entry: a colored state dot and a short label ("Indexed 1,204 notes"). The bar has a fixed
 * height, so the label stays on one line and is ellipsized when the window is too narrow.
 */
@Composable
fun StatusBarItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    indicator: StatusIndicator = StatusIndicator.IDLE
) {
    Row(
        modifier = modifier
            .height(NotekeaDimensions.StatusBarHeight)
            .focusBorder(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(NotekeaDimensions.SpacingS)
                .background(color = indicator.color(colorScheme = MaterialTheme.colorScheme), shape = CircleShape)
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = NotekeaDimensions.SpacingXs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun StatusBarItemPreview() {
    NotekeaPreviewThemes {
        Row {
            StatusBarItem(label = PREVIEW_INDEXED, onClick = {}, indicator = StatusIndicator.ACTIVE)
            StatusBarItem(label = PREVIEW_MODEL, onClick = {})
            StatusBarItem(label = PREVIEW_SYNC, onClick = {}, indicator = StatusIndicator.ERROR)
        }
    }
}

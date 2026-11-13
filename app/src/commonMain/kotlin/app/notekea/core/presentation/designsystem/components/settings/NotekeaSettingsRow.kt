package app.notekea.core.presentation.designsystem.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val ROW_MIN_HEIGHT_DP = 40
private const val PREVIEW_WIDTH_DP = 480
private const val PREVIEW_LABEL = "Open the last vault at startup"
private const val PREVIEW_DETAIL = "Or the welcome screen"

/**
 * One setting of a [NotekeaSettingsGroup]: its [label] (and, smaller, a [detail]) on the left, its [control] on the
 * right, at the row's height.
 */
@Composable
fun NotekeaSettingsRow(
    label: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    control: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN_HEIGHT_DP.dp)
            .padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = NotekeaDimensions.SpacingS
            ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            detail?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        control()
    }
}

@Preview
@Composable
private fun NotekeaSettingsRowPreview() {
    NotekeaPreviewThemes {
        NotekeaSettingsRow(
            label = PREVIEW_LABEL,
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            detail = PREVIEW_DETAIL
        )
    }
}

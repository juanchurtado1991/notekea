package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val CAP_HORIZONTAL_PADDING_DP = 6
private const val CAP_SPACING_DP = 3
private const val CAP_VERTICAL_PADDING_DP = 1
private val PREVIEW_KEYS = listOf("Ctrl", "Shift", "O")

/** A shortcut as keycaps, one small outlined cap per key of [keys] (`Ctrl` `Shift` `O`, or `⇧` `⌘` `O` on macOS). */
@Composable
fun NotekeaKeycaps(
    keys: List<String>,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.extraSmall
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(space = CAP_SPACING_DP.dp)) {
        keys.forEach { key ->
            Text(
                text = key,
                modifier = Modifier
                    .background(color = colors.surfaceVariant, shape = shape)
                    .border(width = NotekeaDimensions.BorderWidth, color = colors.outline, shape = shape)
                    .padding(horizontal = CAP_HORIZONTAL_PADDING_DP.dp, vertical = CAP_VERTICAL_PADDING_DP.dp),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaKeycapsPreview() {
    NotekeaPreviewThemes { NotekeaKeycaps(keys = PREVIEW_KEYS) }
}

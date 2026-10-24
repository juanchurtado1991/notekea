package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val PREVIEW_TEXT = "Cards group related rows, like the recent vaults."
private const val PREVIEW_WIDTH_DP = 320

/** Surface with the 10 dp card radius and a hairline outline, for grouping related content on the background. */
@Composable
fun NotekeaCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
        contentColor = colors.onSurface,
        border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = colors.outline)
    ) {
        Column(modifier = Modifier.padding(all = NotekeaDimensions.SpacingXs), content = content)
    }
}

@Preview
@Composable
private fun NotekeaCardPreview() {
    NotekeaPreviewThemes {
        NotekeaCard(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            Text(
                text = PREVIEW_TEXT,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(all = NotekeaDimensions.SpacingM)
            )
        }
    }
}

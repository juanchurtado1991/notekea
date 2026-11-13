package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val PREVIEW_WIDTH_DP = 480

/**
 * A group of settings, as macOS's System Settings draws them (the developer's choice, 2026-11-11): an optional
 * [header], then rounded on the container's color, one row each with a line between, and an optional [footnote].
 */
@Composable
fun NotekeaSettingsGroup(
    rows: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    header: String? = null,
    footnote: String? = null
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        header?.let {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingM),
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = colors.surfaceContainer,
            border = BorderStroke(
                width = NotekeaDimensions.BorderWidth,
                color = colors.outlineVariant
            )
        ) {
            Column {
                rows.forEachIndexed { index, row ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = NotekeaDimensions.SpacingM)
                                .fillMaxWidth()
                                .height(height = NotekeaDimensions.BorderWidth)
                                .background(color = colors.outlineVariant)
                        )
                    }

                    row()
                }
            }
        }

        footnote?.let {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingM),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaSettingsGroupPreview() {
    NotekeaPreviewThemes {
        NotekeaSettingsGroup(
            rows = listOf(
                {
                    NotekeaSettingsRow(label = "Language") { Text(text = "Español") }
                },
                {
                    NotekeaSettingsRow(
                        label = "Open the last vault at startup",
                        detail = "Or the welcome screen"
                    )
                }
            ),
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            footnote = "System follows your computer's language."
        )
    }
}

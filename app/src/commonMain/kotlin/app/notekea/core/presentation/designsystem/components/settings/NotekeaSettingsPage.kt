package app.notekea.core.presentation.designsystem.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.layout.NotekeaScrollArea
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val PREVIEW_TITLE = "General"

/**
 * One section of Settings as macOS draws a pane: its [title] centered on top, then its groups, scrolling when they do
 * not fit.
 */
@Composable
fun NotekeaSettingsPage(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    NotekeaScrollArea(
        state = scrollState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = scrollState)
                .padding(all = NotekeaDimensions.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme
                    .typography
                    .titleMedium
                    .copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            content()
        }
    }
}

@Preview
@Composable
private fun NotekeaSettingsPagePreview() {
    NotekeaPreviewThemes {
        NotekeaSettingsPage(title = PREVIEW_TITLE) {}
    }
}

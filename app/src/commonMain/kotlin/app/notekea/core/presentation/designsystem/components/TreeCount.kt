package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val PREVIEW_COUNT = "12"

/** How many a folder holds, at the end of its row: [text] in the row's muted [color], as macOS's source list (2A). */
@Composable
internal fun TreeCount(
    text: String,
    color: Color
) {
    Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingS))

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color
    )
}

@Preview
@Composable
private fun TreeCountPreview() {
    NotekeaPreviewThemes {
        TreeCount(
            text = PREVIEW_COUNT,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

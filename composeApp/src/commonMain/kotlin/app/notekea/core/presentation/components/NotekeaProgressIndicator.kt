package app.notekea.core.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes

private const val PREVIEW_LABEL = "Opening vault"
private const val SIZE_DP = 16
private const val STROKE_DP = 2

/**
 * Small inline spinner shown next to the content it is working on (never over it), for operations that take a moment,
 * such as counting the notes of a large vault. [label] is what a screen reader announces.
 */
@Composable
fun NotekeaProgressIndicator(
    label: String,
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier
            .size(size = SIZE_DP.dp)
            .semantics { contentDescription = label },
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = STROKE_DP.dp
    )
}

@Preview
@Composable
private fun NotekeaProgressIndicatorPreview() {
    NotekeaPreviewThemes {
        NotekeaProgressIndicator(label = PREVIEW_LABEL)
    }
}

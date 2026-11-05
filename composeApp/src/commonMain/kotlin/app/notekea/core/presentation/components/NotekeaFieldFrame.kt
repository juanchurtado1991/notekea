package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val PREVIEW_VALUE = "Frank Herbert"

/** The outline of a value in a form's row (a query's rule, a note's property): one control high, its width all there. */
@Composable
fun NotekeaFieldFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = NotekeaDimensions.CompactControlSize)
            .border(
                border = BorderStroke(
                    width = NotekeaDimensions.BorderWidth,
                    color = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.CenterStart
    ) { content() }
}

@Preview
@Composable
private fun NotekeaFieldFramePreview() {
    NotekeaPreviewThemes { NotekeaFieldFrame { Text(text = PREVIEW_VALUE) } }
}

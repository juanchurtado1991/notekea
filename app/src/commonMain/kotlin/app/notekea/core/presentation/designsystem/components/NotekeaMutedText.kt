package app.notekea.core.presentation.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes

/**
 * Secondary text, in the theme's muted color: a hint, a detail under a title, what an empty place says. Cut with an
 * ellipsis past [maxLines].
 */
@Composable
fun NotekeaMutedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview
@Composable
private fun NotekeaMutedTextPreview() {
    NotekeaPreviewThemes { NotekeaMutedText(text = "No notes link here yet") }
}

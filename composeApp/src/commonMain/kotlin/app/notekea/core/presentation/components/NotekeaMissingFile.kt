package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.LocalNotekeaCodeTextStyle
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_LOCATE = "Locate…"
private const val PREVIEW_PATH = "attachments/plate.png"
private const val PREVIEW_TITLE = "This file is missing"

/**
 * The place of a file a note embeds but that cannot be found: [title], the [path] as the note writes it, and a button
 * ([locateLabel]) to choose the file ([onLocate]), which then takes the embed's place.
 */
@Composable
fun NotekeaMissingFile(
    title: String,
    path: String,
    locateLabel: String,
    onLocate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = colors.surfaceVariant,
        border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = colors.outline)
    ) {
        Row(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingM),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = NotekeaIcons.MissingFile,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = colors.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                Text(
                    text = path,
                    style = LocalNotekeaCodeTextStyle.current.copy(color = colors.onSurfaceVariant),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            NotekeaButton(text = locateLabel, onClick = onLocate, style = NotekeaButtonStyle.SECONDARY)
        }
    }
}

@Preview
@Composable
private fun NotekeaMissingFilePreview() {
    NotekeaPreviewThemes {
        NotekeaMissingFile(title = PREVIEW_TITLE, path = PREVIEW_PATH, locateLabel = PREVIEW_LOCATE, onLocate = {})
    }
}

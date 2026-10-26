package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_HINT = "Open with its app"
private const val PREVIEW_NAME = "Menu for the week.pdf"

/**
 * A file attached to a note that Notekea does not show itself (a PDF, audio, an unreadable image): a paperclip, its
 * [name] and, smaller, what a click does ([hint]); clicking opens the file with the system's app ([onOpen]).
 */
@Composable
fun NotekeaFileChip(
    name: String,
    hint: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .focusBorder(color = colors.primary, shape = MaterialTheme.shapes.small)
            .clip(shape = MaterialTheme.shapes.small)
            .clickable(onClickLabel = hint, role = Role.Button, onClick = onOpen),
        shape = MaterialTheme.shapes.small,
        color = colors.surface,
        border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = colors.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingS),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = NotekeaIcons.Attachment,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = colors.primary
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaFileChipPreview() {
    NotekeaPreviewThemes {
        NotekeaFileChip(name = PREVIEW_NAME, hint = PREVIEW_HINT, onOpen = {})
    }
}

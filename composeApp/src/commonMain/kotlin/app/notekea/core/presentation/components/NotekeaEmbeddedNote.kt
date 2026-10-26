package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val ACCENT_WIDTH_DP = 3
private const val PREVIEW_HINT = "Open note"
private const val PREVIEW_TEXT = "Soak the corn flour for ten minutes, then shape the dough into discs."
private const val PREVIEW_TITLE = "Arepas › Dough"

/**
 * A note shown inside another (`![[Other note]]`, as Obsidian embeds it): a bar in the accent color down its left
 * side, its [title] with what a click on it does ([openHint], [onOpen] opens the note), then its [content], the
 * rendered text of the note or of one of its sections.
 */
@Composable
fun NotekeaEmbeddedNote(
    title: String,
    openHint: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = colors.surface,
        border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = colors.outline)
    ) {
        Row(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.width(width = ACCENT_WIDTH_DP.dp).fillMaxHeight().background(color = colors.primary)
            )
            Column(
                modifier = Modifier.padding(all = NotekeaDimensions.SpacingM),
                verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
            ) {
                Title(title = title, openHint = openHint, onOpen = onOpen)
                content()
            }
        }
    }
}

@Composable
private fun Title(
    title: String,
    openHint: String,
    onOpen: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .focusBorder(color = colors.primary, shape = MaterialTheme.shapes.small)
            .clip(shape = MaterialTheme.shapes.small)
            .clickable(onClickLabel = openHint, role = Role.Button, onClick = onOpen),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = NotekeaIcons.Note,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = colors.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = openHint,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Preview
@Composable
private fun NotekeaEmbeddedNotePreview() {
    NotekeaPreviewThemes {
        NotekeaEmbeddedNote(title = PREVIEW_TITLE, openHint = PREVIEW_HINT, onOpen = {}) {
            Text(text = PREVIEW_TEXT, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

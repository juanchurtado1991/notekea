package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaColors
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val ACCENT_WIDTH_DP = 3
private const val PREVIEW_ACTION = "Create note"
private const val PREVIEW_CIRCULAR = "Circular embed: this note is already shown above, so it stops here."
private const val PREVIEW_MISSING = "This note does not exist yet."
private const val PREVIEW_PLAN = "Tasks › Plan"
private const val PREVIEW_TITLE = "Weekend menu"

/**
 * The place of a note another embeds but that cannot be shown (`ROADMAP.md` 1.15), drawn as an embedded note
 * ([NotekeaEmbeddedNote]) whose text is not there: its [title] and bar muted, the [message] saying why, and an action
 * when there is one ([actionLabel], [onAction]: create the note, open it). A [isWarning] notice (an embed that goes
 * around in a circle, or too deep) has a yellow bar.
 */
@Composable
fun NotekeaEmbedNotice(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = colors.surface,
        border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = colors.outline)
    ) {
        // The bar is drawn behind the content, as on an embedded note.
        val bar = if (isWarning) NotekeaColors.warning else colors.onSurfaceVariant
        Box(
            modifier = Modifier.drawBehind {
                drawRect(color = bar, size = Size(width = ACCENT_WIDTH_DP.dp.toPx(), height = size.height))
            }
        ) {
            Column(
                modifier = Modifier.padding(all = NotekeaDimensions.SpacingM),
                verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
            ) {
                NoticeTitle(title = title)
                Text(text = message, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                actionLabel?.let {
                    NotekeaButton(text = it, onClick = onAction, style = NotekeaButtonStyle.SECONDARY)
                }
            }
        }
    }
}

@Composable
private fun NoticeTitle(
    title: String
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = NotekeaIcons.Note,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = muted
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun NotekeaEmbedNoticePreview() {
    NotekeaPreviewThemes {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)) {
            NotekeaEmbedNotice(title = PREVIEW_TITLE, message = PREVIEW_MISSING, actionLabel = PREVIEW_ACTION)
            NotekeaEmbedNotice(title = PREVIEW_PLAN, message = PREVIEW_CIRCULAR, isWarning = true)
        }
    }
}

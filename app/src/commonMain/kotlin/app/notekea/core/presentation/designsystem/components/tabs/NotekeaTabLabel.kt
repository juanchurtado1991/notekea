package app.notekea.core.presentation.designsystem.components.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaIconButton
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val LABEL_MAX_WIDTH_DP = 180
private const val PREVIEW_CLOSE = "Close tab"
private const val PREVIEW_KEY = "a"
private const val PREVIEW_LABEL = "Pricing ideas"
private const val UNSAVED_DOT_DP = 8

/**
 * The tab's icon (primary in the active pane's selected tab), its label cut with an ellipsis when long (semibold when
 * selected), and its close button or unsaved dot.
 */
@Composable
internal fun NotekeaTabLabel(
    tab: NotekeaTabStripItem,
    look: TabLook,
    closeLabel: String,
    onClose: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.padding(
            start = NotekeaDimensions.SpacingM,
            end = NotekeaDimensions.SpacingXs
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tab.icon?.let {
            val isLit = look.isSelected && look.isActive
            val tint = if (isLit) colors.primary else tab.iconTint ?: colors.onSurfaceVariant

            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = tint
            )

            Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingXs))
        }

        Text(
            text = tab.label,
            modifier = Modifier.widthIn(max = LABEL_MAX_WIDTH_DP.dp),
            style = MaterialTheme
                .typography
                .bodyMedium
                .copy(fontWeight = if (look.isSelected) FontWeight.SemiBold else FontWeight.Normal),
            color = if (look.isSelected) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        CloseOrDot(
            unsavedLabel = tab.unsavedLabel,
            look = look,
            closeLabel = closeLabel,
            onClose = onClose
        )
    }
}

/**
 * The tab's close button, seen on the selected or hovered tab and kept in place otherwise; a tab with unsaved changes
 * shows a dot there instead, as VS Code does, until the pointer is on it.
 */
@Composable
private fun CloseOrDot(
    look: TabLook,
    closeLabel: String,
    onClose: () -> Unit,
    unsavedLabel: String?
) {
    Box(contentAlignment = Alignment.Center) {
        val isDotShown = unsavedLabel != null && !look.isHovered
        val isCloseShown = !isDotShown && (look.isSelected || look.isHovered)

        NotekeaIconButton(
            icon = NotekeaIcons.Close,
            label = closeLabel,
            onClick = onClose,
            modifier = Modifier.alpha(alpha = if (isCloseShown) 1f else 0f),
            takesFocus = false
        )

        if (isDotShown) {
            Box(
                modifier = Modifier
                    .size(size = UNSAVED_DOT_DP.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = CircleShape
                    )
                    .semantics { contentDescription = unsavedLabel }
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaTabLabelPreview() {
    NotekeaPreviewThemes {
        NotekeaTabLabel(
            tab = NotekeaTabStripItem(
                key = PREVIEW_KEY,
                label = PREVIEW_LABEL,
                icon = NotekeaIcons.Note
            ),
            look = TabLook(
                isSelected = true,
                isActive = true,
                isHovered = false
            ),
            closeLabel = PREVIEW_CLOSE,
            onClose = {}
        )
    }
}

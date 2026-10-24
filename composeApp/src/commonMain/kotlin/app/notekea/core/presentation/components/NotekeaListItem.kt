package app.notekea.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import app.notekea.core.presentation.theme.NotekeaDimensions
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp

private const val PREVIEW_ARCHIVE = "Archive"
private const val PREVIEW_FOLDER = "Projects/Notekea"
private const val PREVIEW_LOCATE = "Locate…"
private const val PREVIEW_MISSING = "Folder not found"
private const val PREVIEW_LONG_TITLE = "A very long note title that does not fit in the list and gets ellipsized"
private const val PREVIEW_MEETING = "Meeting with Ana"
private const val PREVIEW_PRICING = "Pricing ideas"
private const val PREVIEW_WIDTH_DP = 360

/**
 * Selectable row with a title and optional supporting text (folder, snippet); selection uses the primary container.
 * A row that is not [enabled] cannot be clicked and shows its title muted (a recent vault whose folder is gone);
 * [trailingContent] holds secondary text or actions at the end of the row; [onClickLabel] tells screen readers what a
 * click does ("Open vault").
 */
@Composable
fun NotekeaListItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth().focusBorder(color = colors.primary, shape = MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small,
        color = if (selected) colors.primaryContainer else colors.surface,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = enabled, onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                .padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ListItemTexts(
                title = title,
                enabled = enabled,
                modifier = Modifier.weight(weight = 1f),
                supportingText = supportingText
            )
            trailingContent?.let {
                Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingM))
                it()
            }
        }
    }
}

@Composable
private fun ListItemTexts(
    title: String,
    enabled: Boolean,
    modifier: Modifier,
    supportingText: String?
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) LocalContentColor.current else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        supportingText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaListItemPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaListItem(title = PREVIEW_PRICING, onClick = {}, supportingText = PREVIEW_FOLDER, selected = true)
            NotekeaListItem(title = PREVIEW_MEETING, onClick = {}, supportingText = PREVIEW_FOLDER)
            NotekeaListItem(title = PREVIEW_LONG_TITLE, onClick = {})
            NotekeaListItem(
                title = PREVIEW_ARCHIVE,
                onClick = {},
                supportingText = PREVIEW_MISSING,
                enabled = false,
                trailingContent = {
                    NotekeaButton(
                        text = PREVIEW_LOCATE,
                        onClick = {},
                        style = NotekeaButtonStyle.SECONDARY
                    )
                }
            )
        }
    }
}

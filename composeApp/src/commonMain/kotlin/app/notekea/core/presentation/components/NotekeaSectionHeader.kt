package app.notekea.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val LETTER_SPACING_SP = 0.8
private const val PREVIEW_BOOKMARKS = "Bookmarks"
private const val PREVIEW_COUNT = 4
private const val PREVIEW_FILES = "Files"
private const val PREVIEW_WIDTH_DP = 280

/**
 * A section's header, as desktop inspectors and sidebars draw it: its [title] in small capitals and, when it folds
 * ([onToggle]), a chevron showing whether it [isOpen]; a click folds or opens it. [count] (how many it holds) sits at
 * the end. Only a header with a [shortcutHint] (the shortcut that goes to the section) has a tooltip: the title says the
 * rest, and a tooltip repeating it would cover the section's first row.
 */
@Composable
fun NotekeaSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    isOpen: Boolean = true,
    onToggle: (() -> Unit)? = null,
    count: Int? = null,
    shortcutHint: String? = null
) {
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = NotekeaDimensions.SpacingXs)) {
        if (shortcutHint == null) {
            HeaderRow(title = title, isOpen = isOpen, onToggle = onToggle, count = count)
        } else {
            NotekeaTooltip(label = title, shortcutHint = shortcutHint) {
                HeaderRow(title = title, isOpen = isOpen, onToggle = onToggle, count = count)
            }
        }
    }
}

@Composable
private fun HeaderRow(
    title: String,
    isOpen: Boolean,
    onToggle: (() -> Unit)?,
    count: Int?
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val click = onToggle?.let {
        Modifier.focusProperties { canFocus = false }.clickable(role = Role.Button, onClick = it)
    } ?: Modifier
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = NotekeaDimensions.CompactControlSize)
            .clip(shape = MaterialTheme.shapes.small)
            .then(other = click)
            .padding(horizontal = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        if (onToggle != null) {
            Icon(
                imageVector = if (isOpen) NotekeaIcons.Collapse else NotekeaIcons.Expand,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.BadgeIconSize),
                tint = muted
            )
        } else {
            Spacer(modifier = Modifier.width(width = NotekeaDimensions.BadgeIconSize))
        }
        Text(
            text = title.uppercase(),
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = LETTER_SPACING_SP.sp
            ),
            color = muted
        )
        count?.let { Text(text = it.toString(), style = MaterialTheme.typography.labelSmall, color = muted) }
    }
}

@Preview
@Composable
private fun NotekeaSectionHeaderPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaSectionHeader(title = PREVIEW_BOOKMARKS, onToggle = {}, count = PREVIEW_COUNT)
            NotekeaSectionHeader(title = PREVIEW_FILES)
        }
    }
}

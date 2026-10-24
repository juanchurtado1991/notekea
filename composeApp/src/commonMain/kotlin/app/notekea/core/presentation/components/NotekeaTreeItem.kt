package app.notekea.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_COUNT = "12"
private const val PREVIEW_FOLDER = "Projects"
private const val PREVIEW_GARDEN = "Garden"
private const val PREVIEW_GARDEN_COUNT = "1"
private const val PREVIEW_LONG = "A very long note title that never fits in the sidebar"
private const val PREVIEW_NOTE = "Tomato plan"
private const val PREVIEW_RENAMED = "Tomato plan 2027"
private const val PREVIEW_WIDTH_DP = 260

/**
 * One row of a tree (the vault sidebar): indentation by [depth], a chevron for folders ([isExpanded] is null for
 * leaves, which keep the chevron's space so names align), an icon, the [label] and optional [trailingText] (a note
 * count). Rows are not focus stops: the tree is one focusable element moved with the arrow keys, and the row it is on
 * draws the focus ring when [focused]. Rows are transparent, so they sit on the panel that holds the tree. [labelContent] replaces the label, for renaming in place; it must
 * keep its keys (a field consumes Space), or a key leaving it would click the row. Screen readers get
 * the selection and expand / collapse actions.
 */
@Composable
fun NotekeaTreeItem(
    label: String,
    icon: ImageVector,
    depth: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    focused: Boolean = false,
    isExpanded: Boolean? = null,
    onToggleExpanded: (() -> Unit)? = null,
    trailingText: String? = null,
    onClickLabel: String? = null,
    labelContent: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height = NotekeaDimensions.TreeRowHeight)
            .then(if (focused) Modifier.border(NotekeaDimensions.FocusRingWidth, colors.primary, shape) else Modifier)
            .treeSemantics(selected = selected, isExpanded = isExpanded, onToggleExpanded = onToggleExpanded),
        shape = shape,
        color = if (selected) colors.primaryContainer else Color.Transparent,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface
    ) {
        Row(
            modifier = Modifier
                .rowClick(onClick = onClick, onClickLabel = onClickLabel)
                .padding(
                    start = NotekeaDimensions.SpacingXs + NotekeaDimensions.TreeIndent * depth,
                    end = NotekeaDimensions.SpacingS
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Chevron(isExpanded = isExpanded, onToggleExpanded = onToggleExpanded)
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = if (selected) LocalContentColor.current else colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingS))
            Box(modifier = Modifier.weight(weight = 1f)) {
                labelContent?.invoke() ?: TreeLabel(label = label)
            }
            trailingText?.let { TrailingText(text = it) }
        }
    }
}

@Composable
private fun Chevron(
    isExpanded: Boolean?,
    onToggleExpanded: (() -> Unit)?
) {
    val toggle = Modifier.focusProperties { canFocus = false }.let { base ->
        if (onToggleExpanded != null) base.clickable(onClick = onToggleExpanded) else base
    }
    Box(
        modifier = Modifier.size(size = NotekeaDimensions.IconSize).then(other = toggle),
        contentAlignment = Alignment.Center
    ) {
        if (isExpanded != null) {
            Icon(
                imageVector = if (isExpanded) NotekeaIcons.Collapse else NotekeaIcons.Expand,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingXs))
}

@Composable
private fun TreeLabel(
    label: String
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = LocalContentColor.current,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TrailingText(
    text: String
) {
    Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingS))
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Rows are not focus stops: the tree is. */
private fun Modifier.rowClick(
    onClick: () -> Unit,
    onClickLabel: String?
): Modifier = focusProperties { canFocus = false }
    .clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)

private fun Modifier.treeSemantics(
    selected: Boolean,
    isExpanded: Boolean?,
    onToggleExpanded: (() -> Unit)?
): Modifier = semantics {
    this.selected = selected
    if (isExpanded != null && onToggleExpanded != null) {
        val toggle = {
            onToggleExpanded()
            true
        }
        if (isExpanded) collapse(action = toggle) else expand(action = toggle)
    }
}

@Preview
@Composable
private fun NotekeaTreeItemPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaTreeItem(
                label = PREVIEW_FOLDER,
                icon = NotekeaIcons.FolderOpen,
                depth = 0,
                onClick = {},
                isExpanded = true,
                onToggleExpanded = {},
                trailingText = PREVIEW_COUNT
            )
            NotekeaTreeItem(
                label = PREVIEW_GARDEN,
                icon = NotekeaIcons.Folder,
                depth = 1,
                onClick = {},
                isExpanded = false,
                onToggleExpanded = {},
                trailingText = PREVIEW_GARDEN_COUNT
            )
            NotekeaTreeItem(
                label = PREVIEW_NOTE,
                icon = NotekeaIcons.Note,
                depth = 1,
                onClick = {},
                selected = true,
                focused = true
            )
            NotekeaTreeItem(label = PREVIEW_LONG, icon = NotekeaIcons.Note, depth = 1, onClick = {})
        }
    }
}

@Preview
@Composable
private fun NotekeaTreeItemRenamingPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaTreeItem(
                label = PREVIEW_NOTE,
                icon = NotekeaIcons.Note,
                depth = 1,
                onClick = {},
                labelContent = {
                    NotekeaInlineTextField(
                        value = PREVIEW_RENAMED,
                        onValueChange = {},
                        onSubmit = {},
                        onCancel = {}
                    )
                }
            )
            NotekeaTreeItem(label = PREVIEW_LONG, icon = NotekeaIcons.Note, depth = 1, onClick = {}, selected = true)
        }
    }
}

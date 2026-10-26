package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val BACKGROUND_ALPHA = 0.08f
private const val PREVIEW_BODY = "Soak the beans overnight, or the soup takes twice as long."
private const val PREVIEW_TITLE = "Tip"
private const val PREVIEW_WARNING = "Hot oil"
private const val PREVIEW_WIDTH_DP = 480

/**
 * A callout card (an Obsidian `> [!type]` block in Read mode): a background tinted with [color], a header with [icon]
 * and the [title] in that color, and the [content] under it. When [onToggle] is given the callout can be folded: the
 * header shows a chevron and a click on it shows or hides the content ([isExpanded]); its title is then not selectable,
 * so quick clicks fold and unfold it instead of selecting it.
 */
@Composable
fun NotekeaCallout(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true,
    onToggle: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.small
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = color.copy(alpha = BACKGROUND_ALPHA))
            .padding(all = NotekeaDimensions.SpacingM),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        if (onToggle == null) {
            CalloutHeader(title = title, icon = icon, color = color, isExpanded = isExpanded, onToggle = null)
        } else {
            // Clicking a foldable title twice in a row folds and unfolds it, instead of selecting it as a word.
            DisableSelection {
                CalloutHeader(title = title, icon = icon, color = color, isExpanded = isExpanded, onToggle = onToggle)
            }
        }
        if (isExpanded) content()
    }
}

@Composable
private fun CalloutHeader(
    title: String,
    icon: ImageVector,
    color: Color,
    isExpanded: Boolean,
    onToggle: (() -> Unit)?
) {
    val toggle = onToggle?.let { foldToggle(title = title, onToggle = it) } ?: Modifier
    Row(
        modifier = Modifier.fillMaxWidth().then(other = toggle),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = color
        )
        Text(
            text = title,
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
        if (onToggle != null) {
            Icon(
                imageVector = if (isExpanded) NotekeaIcons.Collapse else NotekeaIcons.Expand,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = color
            )
        }
    }
}

/** A click folds without leaving a highlight or focus on the title; the hand cursor shows it can be clicked. */
@Composable
private fun foldToggle(
    title: String,
    onToggle: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return Modifier
        .focusProperties { canFocus = false }
        .pointerHoverIcon(icon = PointerIcon.Hand)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClickLabel = title,
            role = Role.Button,
            onClick = onToggle
        )
}

@Preview
@Composable
private fun NotekeaCalloutPreview() {
    NotekeaPreviewThemes {
        Column(
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
        ) {
            NotekeaCallout(
                title = PREVIEW_TITLE,
                icon = NotekeaIcons.CalloutTip,
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(text = PREVIEW_BODY, style = MaterialTheme.typography.bodyMedium)
            }
            NotekeaCallout(
                title = PREVIEW_WARNING,
                icon = NotekeaIcons.CalloutWarning,
                color = MaterialTheme.colorScheme.tertiary,
                isExpanded = false,
                onToggle = {}
            ) {
                Text(text = PREVIEW_BODY, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

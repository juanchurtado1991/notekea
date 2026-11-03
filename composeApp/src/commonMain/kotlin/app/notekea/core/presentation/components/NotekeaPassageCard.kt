package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val TEXT_LINES = 3
private const val HIGHLIGHT_ALPHA = 0.22f
private const val PREVIEW_TITLE = "Tomato soup"
private const val PREVIEW_FOLDER = "Recipes"
private const val PREVIEW_TEXT = "Ripe tomatoes roasted with garlic. Summer tomatoes from the beds make it sweeter."
private const val PREVIEW_HIGHLIGHT_START = 35

/**
 * A card that quotes a passage (`ROADMAP.md` 2.4, design A: a related note): its [icon], [title] and [subtitle] (the
 * folder), and up to three lines of [text] with its [highlight] on the accent color. The [actions] show on the title's
 * row while the pointer is over the card or the keyboard is on it or on one of them, so Tab reaches them; a click or Enter on the card goes to [onClick].
 */
@Composable
fun NotekeaPassageCard(
    title: String,
    subtitle: String,
    text: String,
    highlight: IntRange,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    var hasFocus by remember { mutableStateOf(value = false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { hasFocus = it.hasFocus }
            .clip(shape = shape)
            .border(width = NotekeaDimensions.BorderWidth, color = colors.outlineVariant, shape = shape)
            .focusBorder(color = colors.primary, shape = shape)
            .background(color = if (isHovered) colors.surfaceVariant else colors.surface)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(all = NotekeaDimensions.SpacingS),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        TitleRow(title = title, icon = icon, showsActions = isHovered || hasFocus, actions = actions)
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                maxLines = 1
            )
        }
        Passage(text = text, highlight = highlight)
    }
}

/** The card's icon and title, and its actions while [showsActions]. */
@Composable
private fun TitleRow(
    title: String,
    icon: ImageVector,
    showsActions: Boolean,
    actions: @Composable RowScope.() -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            modifier = Modifier.padding(start = NotekeaDimensions.SpacingXs).weight(weight = 1f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (showsActions) actions()
    }
}

@Composable
private fun Passage(
    text: String,
    highlight: IntRange
) {
    val colors = MaterialTheme.colorScheme
    val lit = highlight.first.coerceIn(
        minimumValue = 0,
        maximumValue = text.length
    )..highlight.last.coerceIn(minimumValue = -1, maximumValue = text.length - 1)
    Text(
        text = buildAnnotatedString {
            append(text.substring(startIndex = 0, endIndex = lit.first))
            withStyle(
                style = SpanStyle(background = colors.tertiary.copy(alpha = HIGHLIGHT_ALPHA), color = colors.onSurface)
            ) {
                append(text.substring(startIndex = lit.first, endIndex = lit.last + 1))
            }
            append(text.substring(startIndex = lit.last + 1))
        },
        style = MaterialTheme.typography.bodySmall,
        color = colors.onSurfaceVariant,
        maxLines = TEXT_LINES,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview
@Composable
private fun NotekeaPassageCardPreview() {
    NotekeaPreviewThemes {
        NotekeaPassageCard(
            title = PREVIEW_TITLE,
            subtitle = PREVIEW_FOLDER,
            text = PREVIEW_TEXT,
            highlight = PREVIEW_HIGHLIGHT_START..PREVIEW_TEXT.lastIndex,
            icon = NotekeaIcons.Note,
            onClick = {}
        )
    }
}

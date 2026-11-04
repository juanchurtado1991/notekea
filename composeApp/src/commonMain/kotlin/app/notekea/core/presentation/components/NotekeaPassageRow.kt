package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val TEXT_LINES = 2
private const val HIGHLIGHT_ALPHA = 0.18f
private const val HOVER_ALPHA = 0.05f
private const val ICON_DP = 14
private const val TITLE_ROW_DP = 22
private const val ROW_VERTICAL_DP = 6

// The folder gives way to the title: it never takes more than this.
private const val SUBTITLE_MAX_DP = 150
private const val PREVIEW_TITLE = "Tomato soup"
private const val PREVIEW_FOLDER = "Recipes"
private const val PREVIEW_TEXT = "Ripe tomatoes roasted with garlic. Summer tomatoes from the beds make it sweeter."
private const val PREVIEW_HIGHLIGHT_START = 35

/**
 * A row of a list that quotes a passage, as an inspector lists things (`ROADMAP.md` 2.4, 2.5, design A): its [icon],
 * [title] and, beside it in the muted color, [subtitle] (the folder); below, up to two lines of [text] with its
 * [highlight] on the accent color. No border: the row tints softly under the pointer, and its [actions] show at the
 * end of the title's row while the pointer is over it or the keyboard is on it or on one of them, so Tab reaches them.
 * A click or Enter on the row goes to [onClick].
 */
@Composable
fun NotekeaPassageRow(
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
    val shape = MaterialTheme.shapes.small
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    var hasFocus by remember { mutableStateOf(value = false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = NotekeaDimensions.SpacingXs)
            .onFocusChanged { hasFocus = it.hasFocus }
            .clip(shape = shape)
            .focusBorder(color = colors.primary, shape = shape)
            .background(
                color = if (isHovered ||
                    hasFocus
                ) {
                    colors.onSurface.copy(alpha = HOVER_ALPHA)
                } else {
                    Color.Transparent
                }
            )
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = NotekeaDimensions.SpacingS, vertical = ROW_VERTICAL_DP.dp)
    ) {
        TitleRow(
            title = title,
            subtitle = subtitle,
            icon = icon,
            showsActions = isHovered || hasFocus,
            actions = actions
        )
        Passage(text = text, highlight = highlight)
    }
}

/** The row's icon, title and folder, and its actions at the end while [showsActions]. */
@Composable
private fun TitleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    showsActions: Boolean,
    actions: @Composable RowScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.height(height = TITLE_ROW_DP.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = ICON_DP.dp),
            tint = colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingS))
        Row(modifier = Modifier.weight(weight = 1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                modifier = Modifier.weight(weight = 1f, fill = false),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    modifier = Modifier
                        .padding(start = NotekeaDimensions.SpacingS)
                        .widthIn(max = SUBTITLE_MAX_DP.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
                append(text.substring(startIndex = lit.first, endIndex = (lit.last + 1).coerceAtLeast(lit.first)))
            }
            append(text.substring(startIndex = (lit.last + 1).coerceAtLeast(lit.first)))
        },
        modifier = Modifier.padding(start = (ICON_DP.dp + NotekeaDimensions.SpacingS)),
        style = MaterialTheme.typography.bodySmall,
        color = colors.onSurfaceVariant,
        maxLines = TEXT_LINES,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview
@Composable
private fun NotekeaPassageRowPreview() {
    NotekeaPreviewThemes {
        NotekeaPassageRow(
            title = PREVIEW_TITLE,
            subtitle = PREVIEW_FOLDER,
            text = PREVIEW_TEXT,
            highlight = PREVIEW_HIGHLIGHT_START..PREVIEW_TEXT.lastIndex,
            icon = NotekeaIcons.Note,
            onClick = {}
        )
    }
}

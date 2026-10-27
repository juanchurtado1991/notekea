package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val ICON_DP = 12
private const val PREVIEW_CURSOR = "16:4"
private const val PREVIEW_ENCODING = "UTF-8"
private const val PREVIEW_LOADING = "Loading notes…"
private const val PREVIEW_NOTE = "Tasks and callouts"
private const val PREVIEW_VAULT = "Playground Vault"
private const val PROGRESS_HEIGHT_DP = 3
private const val PROGRESS_WIDTH_DP = 120
private const val SEPARATOR_HEIGHT_DP = 12

/**
 * The window's bottom bar, as IDEs draw it ([NotekeaDimensions.StatusBarHeight] tall, under a hairline): [start] holds
 * where the user is, [end] the work in progress and the facts of what is open.
 */
@Composable
fun NotekeaStatusBar(
    start: @Composable RowScope.() -> Unit,
    end: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = NotekeaDimensions.BorderWidth)
                .background(color = MaterialTheme.colorScheme.outline)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = NotekeaDimensions.StatusBarHeight)
                .padding(horizontal = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(weight = 1f),
                verticalAlignment = Alignment.CenterVertically,
                content = start
            )
            Row(verticalAlignment = Alignment.CenterVertically, content = end)
        }
    }
}

/**
 * A short fact of the status bar, in the muted color, with its [icon] if any and its [tooltip] on hover if any. One
 * that does something on a click ([onClick]: a folder of the path shown in Files, the zoom reset) shows a hand and a
 * faint fill under the pointer.
 */
@Composable
fun NotekeaStatusLabel(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tooltip: String? = null,
    onClick: (() -> Unit)? = null
) {
    val click = onClick?.let {
        Modifier
            .clip(shape = MaterialTheme.shapes.small)
            .pointerHoverIcon(icon = PointerIcon.Hand)
            .focusProperties { canFocus = false }
            .clickable(role = Role.Button, onClick = it)
    } ?: Modifier
    val label = @Composable {
        Row(
            modifier = modifier.then(other = click).padding(horizontal = NotekeaDimensions.SpacingS),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(size = ICON_DP.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    if (tooltip == null) label() else NotekeaTooltip(label = tooltip) { label() }
}

/** A thin upright line between two facts of the status bar. */
@Composable
fun NotekeaStatusSeparator() {
    Box(
        modifier = Modifier
            .width(width = NotekeaDimensions.BorderWidth)
            .height(height = SEPARATOR_HEIGHT_DP.dp)
            .background(color = MaterialTheme.colorScheme.outline)
    )
}

/** Work in progress: what it is ([label]) and a thin moving bar, as long as the work has no known end. */
@Composable
fun NotekeaStatusProgress(
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        NotekeaStatusLabel(text = label)
        LinearProgressIndicator(
            modifier = Modifier.width(width = PROGRESS_WIDTH_DP.dp).height(height = PROGRESS_HEIGHT_DP.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingS))
    }
}

@Preview
@Composable
private fun NotekeaStatusBarPreview() {
    NotekeaPreviewThemes {
        NotekeaStatusBar(
            start = {
                NotekeaStatusLabel(text = PREVIEW_VAULT, icon = NotekeaIcons.SwitchVault)
                NotekeaStatusLabel(text = PREVIEW_NOTE, icon = NotekeaIcons.Note)
            },
            end = {
                NotekeaStatusProgress(label = PREVIEW_LOADING)
                NotekeaStatusLabel(text = PREVIEW_CURSOR)
                NotekeaStatusSeparator()
                NotekeaStatusLabel(text = PREVIEW_ENCODING)
            }
        )
    }
}

@Preview
@Composable
private fun NotekeaStatusLabelPreview() {
    NotekeaPreviewThemes { NotekeaStatusLabel(text = PREVIEW_NOTE, icon = NotekeaIcons.Note) }
}

@Preview
@Composable
private fun NotekeaStatusSeparatorPreview() {
    NotekeaPreviewThemes { NotekeaStatusSeparator() }
}

@Preview
@Composable
private fun NotekeaStatusProgressPreview() {
    NotekeaPreviewThemes { NotekeaStatusProgress(label = PREVIEW_LOADING) }
}

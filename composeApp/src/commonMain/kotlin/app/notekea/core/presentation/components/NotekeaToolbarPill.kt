package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_BOLD = "Bold"
private const val PREVIEW_ITALIC = "Italic"
private const val PREVIEW_LINK = "Link to a note"
private const val PREVIEW_MORE = "More"

/**
 * A row of toolbar buttons in one capsule (the editor's formatting bar): the theme's soft surface with the 6 dp
 * corners of the other controls, and
 * [groups] of related buttons separated by thin lines, so a long row reads as a few groups.
 */
@Composable
fun NotekeaToolbarPill(
    groups: List<@Composable RowScope.() -> Unit>,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(size = NotekeaDimensions.ControlRadius)
    Row(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = shape)
            .padding(horizontal = NotekeaDimensions.SpacingXs, vertical = NotekeaDimensions.SpacingXs / 2),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        groups.forEachIndexed { index, group ->
            if (index > 0) GroupDivider()
            group()
        }
    }
}

/** The thin line between two groups. */
@Composable
private fun GroupDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = NotekeaDimensions.SpacingXs / 2)
            .width(width = NotekeaDimensions.BorderWidth)
            .height(height = NotekeaDimensions.IconSize + NotekeaDimensions.SpacingXs)
            .background(color = MaterialTheme.colorScheme.outline)
    )
}

@Preview
@Composable
private fun NotekeaToolbarPillPreview() {
    NotekeaPreviewThemes {
        NotekeaToolbarPill(
            groups = listOf(
                {
                    NotekeaIconButton(icon = NotekeaIcons.Bold, label = PREVIEW_BOLD, onClick = {})
                    NotekeaIconButton(icon = NotekeaIcons.Italic, label = PREVIEW_ITALIC, onClick = {})
                },
                { NotekeaIconButton(icon = NotekeaIcons.NoteLink, label = PREVIEW_LINK, onClick = {}) },
                { NotekeaIconButton(icon = NotekeaIcons.More, label = PREVIEW_MORE, onClick = {}) }
            )
        )
    }
}

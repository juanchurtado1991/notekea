package app.notekea.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_HEIGHT_DP = 160
private const val PREVIEW_MODIFIED = "Date modified, newest first"
private const val PREVIEW_NAME = "Name"
private const val PREVIEW_SORT = "Sort notes: name"
private const val PREVIEW_WIDTH_DP = 260

/**
 * An icon button that opens a menu of [sections] below itself, for a setting with a few choices (the tree's sort):
 * [icon] and [label] show the current choice, and items with [NotekeaMenuItem.isChecked] mark it in the menu.
 * The menu is a [NotekeaContextMenuArea], so it looks and closes like every other menu.
 */
@Composable
fun NotekeaMenuButton(
    icon: ImageVector,
    label: String,
    sections: List<List<NotekeaMenuItem>>,
    modifier: Modifier = Modifier
) {
    val menuState = remember { NotekeaContextMenuState() }
    val belowButton = with(LocalDensity.current) { Offset(x = 0f, y = NotekeaDimensions.CompactControlSize.toPx()) }
    NotekeaContextMenuArea(sections = sections, modifier = modifier, state = menuState) {
        NotekeaIconButton(icon = icon, label = label, onClick = { menuState.open(at = belowButton) })
    }
}

@Preview
@Composable
private fun NotekeaMenuButtonPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            NotekeaMenuButton(
                icon = NotekeaIcons.SortByName,
                label = PREVIEW_SORT,
                sections = listOf(
                    listOf(
                        NotekeaMenuItem(label = PREVIEW_NAME, onClick = {}, isChecked = true),
                        NotekeaMenuItem(label = PREVIEW_MODIFIED, onClick = {}, isChecked = false)
                    )
                )
            )
        }
    }
}

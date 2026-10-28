package app.notekea.core.presentation.gallery

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaCard
import app.notekea.core.presentation.components.NotekeaConfirmDialog
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.NotekeaGridSizePicker
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaMarkdown
import app.notekea.core.presentation.components.NotekeaMenuButton
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.NotekeaSplitPane
import app.notekea.core.presentation.components.PaneSide
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

@Composable
internal fun MarkdownSection() {
    NotekeaCard {
        NotekeaMarkdown(content = MARKDOWN_SAMPLE, modifier = Modifier.padding(all = NotekeaDimensions.SpacingM))
    }
}

@Composable
internal fun ContextMenuSection() {
    val sections = listOf(
        listOf(
            NotekeaMenuItem(
                label = BUTTON_NEW_NOTE,
                onClick = {},
                icon = NotekeaIcons.NewNote,
                shortcutHint = BUTTON_NEW_NOTE_SHORTCUT
            )
        ),
        listOf(
            NotekeaMenuItem(
                label = MENU_RENAME,
                onClick = {},
                icon = NotekeaIcons.Rename,
                shortcutHint = MENU_RENAME_SHORTCUT
            ),
            NotekeaMenuItem(label = MENU_REVEAL, onClick = {}, icon = NotekeaIcons.Reveal)
        ),
        listOf(
            NotekeaMenuItem(
                label = BUTTON_DELETE,
                onClick = {},
                icon = NotekeaIcons.Delete,
                shortcutHint = MENU_DELETE_SHORTCUT,
                isDestructive = true
            )
        )
    )
    NotekeaContextMenuArea(sections = sections) {
        NotekeaCard {
            Text(text = MENU_AREA, modifier = Modifier.padding(all = NotekeaDimensions.SpacingM))
        }
    }
    MenuButtonSample()
}

@Composable
private fun MenuButtonSample() {
    var isByName by remember { mutableStateOf(value = true) }
    NotekeaMenuButton(
        icon = if (isByName) NotekeaIcons.SortByName else NotekeaIcons.SortByModified,
        label = if (isByName) MENU_SORTED_BY_NAME else MENU_SORTED_BY_DATE,
        sections = listOf(
            listOf(
                NotekeaMenuItem(label = MENU_SORT_NAME, onClick = { isByName = true }, isChecked = isByName),
                NotekeaMenuItem(label = MENU_SORT_DATE, onClick = { isByName = false }, isChecked = !isByName)
            ),
            listOf(
                NotekeaMenuItem(
                    label = MENU_TABLE,
                    onClick = {},
                    supportingText = MENU_TABLE_HINT,
                    submenu = { onChosen ->
                        NotekeaGridSizePicker(
                            columns = GRID_COLUMNS,
                            rows = GRID_ROWS,
                            label = { columns, rows -> "$columns × $rows" },
                            onPick = { _, _ -> onChosen() }
                        )
                    }
                )
            )
        )
    )
}

@Composable
internal fun ConfirmDialogSection() {
    var isOpen by remember { mutableStateOf(value = false) }
    NotekeaButton(text = DIALOG_OPEN, onClick = { isOpen = true }, style = NotekeaButtonStyle.SECONDARY)
    if (isOpen) {
        NotekeaConfirmDialog(
            title = DIALOG_TITLE,
            message = DIALOG_MESSAGE,
            confirmLabel = BUTTON_DELETE,
            cancelLabel = BUTTON_CANCEL,
            buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
            onConfirm = { isOpen = false },
            onDismiss = { isOpen = false },
            isDestructive = true
        )
    }
}

@Composable
internal fun SplitPaneSection() {
    var isVisible by remember { mutableStateOf(value = true) }
    var width by remember { mutableStateOf(value = SPLIT_MIN_DP.dp) }
    var side by remember { mutableStateOf(value = PaneSide.START) }
    var isOverlay by remember { mutableStateOf(value = false) }
    Row {
        NotekeaIconButton(icon = NotekeaIcons.HideSidebar, label = SPLIT_TOGGLE, onClick = { isVisible = !isVisible })
        NotekeaIconButton(
            icon = NotekeaIcons.SidePanel,
            label = SPLIT_SIDE,
            onClick = { side = if (side == PaneSide.START) PaneSide.END else PaneSide.START }
        )
        NotekeaIconButton(icon = NotekeaIcons.Outline, label = SPLIT_OVERLAY, onClick = { isOverlay = !isOverlay })
    }
    NotekeaCard {
        NotekeaSplitPane(
            panelWidth = width,
            onPanelWidthChange = { width = it },
            isPanelVisible = isVisible,
            minPanelWidth = SPLIT_MIN_DP.dp,
            maxPanelWidth = SPLIT_MAX_DP.dp,
            panel = { Text(text = SPLIT_START, modifier = Modifier.padding(all = NotekeaDimensions.SpacingS)) },
            content = { Text(text = SPLIT_END, modifier = Modifier.padding(all = NotekeaDimensions.SpacingS)) },
            modifier = Modifier.height(height = SPLIT_HEIGHT_DP.dp),
            panelSide = side,
            isOverlay = isOverlay
        )
    }
}

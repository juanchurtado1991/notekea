package app.notekea.core.presentation.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaCard
import app.notekea.core.presentation.components.NotekeaCheckbox
import app.notekea.core.presentation.components.NotekeaFindBar
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaInlineTextField
import app.notekea.core.presentation.components.NotekeaListItem
import app.notekea.core.presentation.components.NotekeaSearchButton
import app.notekea.core.presentation.components.NotekeaSkeletonRow
import app.notekea.core.presentation.components.NotekeaTextField
import app.notekea.core.presentation.components.NotekeaTitleField
import app.notekea.core.presentation.components.NotekeaTreeItem
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

@Composable
internal fun ButtonsSection() {
    Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        NotekeaButton(text = BUTTON_OPEN_FOLDER, onClick = {}, shortcutHint = BUTTON_OPEN_SHORTCUT)
        NotekeaButton(text = BUTTON_CANCEL, onClick = {}, style = NotekeaButtonStyle.SECONDARY)
        NotekeaButton(text = BUTTON_DELETE, onClick = {}, style = NotekeaButtonStyle.DESTRUCTIVE)
        NotekeaButton(text = BUTTON_REMOVE, onClick = {}, style = NotekeaButtonStyle.TEXT)
        NotekeaButton(text = BUTTON_DISABLED, onClick = {}, enabled = false)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        NotekeaIconButton(
            icon = NotekeaIcons.NewNote,
            label = BUTTON_NEW_NOTE,
            onClick = {},
            shortcutHint = BUTTON_NEW_NOTE_SHORTCUT
        )
        NotekeaIconButton(icon = NotekeaIcons.NewFolder, label = BUTTON_NEW_FOLDER, onClick = {})
        NotekeaIconButton(icon = NotekeaIcons.SortByName, label = BUTTON_SORT, onClick = {}, enabled = false)
    }
}

/** The inputs of a note page: its editable title and the Find in note bar. */
@Composable
private fun PageInputsSamples() {
    var title by remember { mutableStateOf(value = NOTE_PRICING) }
    var findQuery by remember { mutableStateOf(value = "") }
    NotekeaTitleField(value = title, onCommit = { title = it }, label = NOTE_PRICING)
    NotekeaSearchButton(label = SEARCH_LABEL, onClick = {}, shortcutHint = SEARCH_SHORTCUT)
    var isTaskDone by remember { mutableStateOf(value = false) }
    NotekeaCheckbox(checked = isTaskDone, onClick = { isTaskDone = !isTaskDone }, label = SEARCH_LABEL)
    NotekeaFindBar(
        query = findQuery,
        onQueryChange = { findQuery = it },
        matchLabel = if (findQuery.isEmpty()) "" else FIND_COUNT,
        onNext = {},
        onPrevious = {},
        onClose = { findQuery = "" }
    )
}

@Composable
internal fun InputsSection() {
    var query by remember { mutableStateOf(value = "") }
    var selectedNote by remember { mutableIntStateOf(value = 0) }
    PageInputsSamples()
    NotekeaTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = SAMPLE_PLACEHOLDER
    )
    NotekeaCard {
        NotekeaListItem(
            title = NOTE_PRICING,
            onClick = { selectedNote = 0 },
            supportingText = NOTE_FOLDER_PROJECTS,
            selected = selectedNote == 0
        )
        NotekeaListItem(
            title = NOTE_MEETING,
            onClick = { selectedNote = 1 },
            supportingText = NOTE_FOLDER_JOURNAL,
            selected = selectedNote == 1,
            trailingContent = { Text(text = NOTE_EDITED, style = MaterialTheme.typography.bodySmall) }
        )
        NotekeaListItem(
            title = NOTE_ARCHIVE,
            onClick = {},
            supportingText = NOTE_MISSING,
            enabled = false,
            trailingContent = {
                NotekeaButton(
                    text = BUTTON_LOCATE,
                    onClick = {},
                    style = NotekeaButtonStyle.SECONDARY
                )
            }
        )
    }
}

@Composable
internal fun TreeSection() {
    var isExpanded by remember { mutableStateOf(value = true) }
    Column(modifier = Modifier.width(width = TREE_WIDTH_DP.dp)) {
        NotekeaTreeItem(
            label = TREE_FOLDER_PROJECTS,
            icon = if (isExpanded) NotekeaIcons.FolderOpen else NotekeaIcons.Folder,
            depth = 0,
            onClick = { isExpanded = !isExpanded },
            isExpanded = isExpanded,
            onToggleExpanded = { isExpanded = !isExpanded },
            trailingText = TREE_COUNT_PROJECTS
        )
        if (isExpanded) TreeSectionChildren()
        NotekeaSkeletonRow(widthFraction = SKELETON_SHORT_FRACTION)
        NotekeaSkeletonRow(widthFraction = SKELETON_LONG_FRACTION, depth = 1)
    }
}

@Composable
private fun TreeSectionChildren() {
    var renamed by remember { mutableStateOf(value = TREE_NOTE_RENAMED) }
    NotekeaTreeItem(label = TREE_FOLDER_GARDEN, icon = NotekeaIcons.Folder, depth = 1, onClick = {}, isExpanded = false)
    NotekeaTreeItem(
        label = TREE_NOTE_TOMATO,
        icon = NotekeaIcons.Note,
        depth = 1,
        onClick = {},
        selected = true,
        focused = true
    )
    NotekeaTreeItem(
        label = TREE_NOTE_TOMATO,
        icon = NotekeaIcons.Note,
        depth = 1,
        onClick = {},
        labelContent = {
            NotekeaInlineTextField(value = renamed, onValueChange = { renamed = it }, onSubmit = {}, onCancel = {})
        }
    )
}

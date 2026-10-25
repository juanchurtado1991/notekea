package app.notekea.core.presentation.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.ErrorBanner
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaCard
import app.notekea.core.presentation.components.NotekeaConfirmDialog
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.NotekeaFindBar
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaInlineTextField
import app.notekea.core.presentation.components.NotekeaWordmark
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaListItem
import app.notekea.core.presentation.components.NotekeaMarkdown
import app.notekea.core.presentation.components.NotekeaMenuButton
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.NotekeaProgressIndicator
import app.notekea.core.presentation.components.NotekeaSkeletonRow
import app.notekea.core.presentation.components.NotekeaSplitPane
import app.notekea.core.presentation.components.NotekeaTab
import app.notekea.core.presentation.components.NotekeaTabRow
import app.notekea.core.presentation.components.NotekeaTextField
import app.notekea.core.presentation.components.NotekeaTitleField
import app.notekea.core.presentation.components.NotekeaTreeItem
import app.notekea.core.presentation.components.StatusBarItem
import app.notekea.core.presentation.components.StatusIndicator
import app.notekea.core.presentation.theme.LocalNotekeaCodeTextStyle
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.core.presentation.theme.NotekeaTheme

/*
 * Developer-only gallery of the design system, rendered in the light and dark themes side by side.
 * Opened with `./gradlew :composeApp:run -Pgallery`; never reachable from a packaged build, so its sample texts are
 * constants instead of string resources (ARCHITECTURE.md §7).
 */

private const val BADGE_SAMPLE_COUNT = 12
private const val BADGE_TAB_INDEX = 1
private const val BUTTON_CANCEL = "Cancel"
private const val BUTTON_DELETE = "Delete note"
private const val BUTTON_DISABLED = "Disabled"
private const val BUTTON_LOAD_THEIRS = "Load theirs"
private const val BUTTON_LOCATE = "Locate…"
private const val BUTTON_NEW_FOLDER = "New folder"
private const val BUTTON_NEW_NOTE = "New note"
private const val BUTTON_NEW_NOTE_SHORTCUT = "Ctrl+N"
private const val BUTTON_OPEN_FOLDER = "Open folder…"
private const val BUTTON_OPEN_SHORTCUT = "Ctrl+O"
private const val BUTTON_REMOVE = "Remove"
private const val BUTTON_SORT = "Sort by name"
private const val FIND_COUNT = "1 of 3"
private const val MARKDOWN_SAMPLE =
    "## Read mode\n\nRendered **Markdown** with a [link](https://example.com) and code:\n\n" +
        "```kotlin\nfun greet(name: String) = \"Hello, ${'$'}name\"\n```\n"
private const val MENU_AREA = "Right-click for the context menu"
private const val MENU_DELETE_SHORTCUT = "Delete"
private const val MENU_RENAME = "Rename"
private const val MENU_RENAME_SHORTCUT = "F2"
private const val MENU_REVEAL = "Show in Files"
private const val MENU_SORTED_BY_DATE = "Sort notes: date modified"
private const val MENU_SORTED_BY_NAME = "Sort notes: name"
private const val MENU_SORT_DATE = "Date modified, newest first"
private const val MENU_SORT_NAME = "Name"
private const val DIALOG_MESSAGE = "“Tomato plan” will be moved to the trash. You can restore it from there."
private const val DIALOG_OPEN = "Delete note…"
private const val DIALOG_TITLE = "Delete this note?"
private const val SPLIT_END = "Editor"
private const val SPLIT_HEIGHT_DP = 120
private const val SPLIT_MAX_DP = 320
private const val SPLIT_MIN_DP = 120
private const val SPLIT_START = "Sidebar — drag the edge"
private const val SPLIT_TOGGLE = "Hide or show the sidebar"
private const val CODE_SAMPLE = "fun main() = println(\"Notekea\")"
private const val DARK_TITLE = "Dark"
private const val EMPTY_MESSAGE = "Create your first note to get started."
private const val EMPTY_TITLE = "This folder has no notes yet"
private const val LIGHT_TITLE = "Light"
private const val NOTE_ARCHIVE = "Archive"
private const val NOTE_EDITED = "2 days ago"
private const val NOTE_FOLDER_JOURNAL = "Journal"
private const val NOTE_FOLDER_PROJECTS = "Projects/Notekea"
private const val NOTE_MEETING = "Meeting with Ana"
private const val NOTE_MISSING = "Folder not found"
private const val NOTE_PRICING = "Pricing ideas"
private const val PROGRESS_LABEL = "Opening vault…"
private const val SAMPLE_ERROR = "This note changed outside Notekea."
private const val SAMPLE_PLACEHOLDER = "Search notes…"
private const val STATUS_INDEXED = "Indexed 1,204 notes"
private const val STATUS_MODEL = "Model: ready"
private const val STATUS_SYNC = "Sync: conflict"
private const val SKELETON_LONG_FRACTION = 0.7f
private const val SKELETON_SHORT_FRACTION = 0.5f
private const val TREE_COUNT_PROJECTS = "12"
private const val TREE_FOLDER_GARDEN = "Garden"
private const val TREE_FOLDER_PROJECTS = "Projects"
private const val TREE_NOTE_RENAMED = "Tomato plan 2027"
private const val TREE_NOTE_TOMATO = "Tomato plan"
private const val TREE_WIDTH_DP = 260
private val SAMPLE_TABS = listOf("Related", "Backlinks", "Outline", "Chat")

@Composable
fun DesignSystemGallery() {
    Row(modifier = Modifier.fillMaxSize()) {
        GalleryColumn(title = LIGHT_TITLE, darkTheme = false, modifier = Modifier.weight(weight = 1f))
        GalleryColumn(title = DARK_TITLE, darkTheme = true, modifier = Modifier.weight(weight = 1f))
    }
}

@Composable
private fun GalleryColumn(
    title: String,
    darkTheme: Boolean,
    modifier: Modifier
) {
    NotekeaTheme(darkTheme = darkTheme) {
        Surface(modifier = modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .verticalScroll(state = rememberScrollState())
                    .padding(all = NotekeaDimensions.SpacingXl),
                verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
            ) {
                Text(text = title, style = MaterialTheme.typography.headlineMedium)
                NotekeaWordmark()
                ButtonsSection()
                InputsSection()
                TreeSection()
                MarkdownSection()
                ContextMenuSection()
                ConfirmDialogSection()
                SplitPaneSection()
                TabsSection()
                FeedbackSection()
                Text(text = CODE_SAMPLE, style = LocalNotekeaCodeTextStyle.current)
            }
        }
    }
}

@Composable
private fun ButtonsSection() {
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
private fun InputsSection() {
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
private fun TreeSection() {
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

@Composable
private fun MarkdownSection() {
    NotekeaCard {
        NotekeaMarkdown(content = MARKDOWN_SAMPLE, modifier = Modifier.padding(all = NotekeaDimensions.SpacingM))
    }
}

@Composable
private fun ContextMenuSection() {
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
            )
        )
    )
}

@Composable
private fun ConfirmDialogSection() {
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
private fun SplitPaneSection() {
    var isVisible by remember { mutableStateOf(value = true) }
    var width by remember { mutableStateOf(value = SPLIT_MIN_DP.dp) }
    NotekeaIconButton(icon = NotekeaIcons.HideSidebar, label = SPLIT_TOGGLE, onClick = { isVisible = !isVisible })
    NotekeaCard {
        NotekeaSplitPane(
            startWidth = width,
            onStartWidthChange = { width = it },
            isStartVisible = isVisible,
            minStartWidth = SPLIT_MIN_DP.dp,
            maxStartWidth = SPLIT_MAX_DP.dp,
            start = { Text(text = SPLIT_START, modifier = Modifier.padding(all = NotekeaDimensions.SpacingS)) },
            end = { Text(text = SPLIT_END, modifier = Modifier.padding(all = NotekeaDimensions.SpacingS)) },
            modifier = Modifier.height(height = SPLIT_HEIGHT_DP.dp)
        )
    }
}

@Composable
private fun TabsSection() {
    var selectedTab by remember { mutableIntStateOf(value = 0) }
    NotekeaTabRow(selectedTabIndex = selectedTab) {
        SAMPLE_TABS.forEachIndexed { index, label ->
            NotekeaTab(
                text = label,
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                badgeCount = BADGE_SAMPLE_COUNT.takeIf { index == BADGE_TAB_INDEX }
            )
        }
    }
}

@Composable
private fun FeedbackSection() {
    Row {
        StatusBarItem(label = STATUS_INDEXED, onClick = {}, indicator = StatusIndicator.ACTIVE)
        StatusBarItem(label = STATUS_MODEL, onClick = {})
        StatusBarItem(label = STATUS_SYNC, onClick = {}, indicator = StatusIndicator.ERROR)
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaProgressIndicator(label = PROGRESS_LABEL)
        Text(text = PROGRESS_LABEL, style = MaterialTheme.typography.bodySmall)
    }
    ErrorBanner(
        message = SAMPLE_ERROR,
        onDismiss = {},
        action = { NotekeaButton(text = BUTTON_LOAD_THEIRS, onClick = {}, style = NotekeaButtonStyle.SECONDARY) }
    )
    EmptyState(
        title = EMPTY_TITLE,
        message = EMPTY_MESSAGE,
        action = { NotekeaButton(text = BUTTON_NEW_NOTE, onClick = {}) }
    )
}

package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.presentation.components.NotekeaToolRail
import app.notekea.core.presentation.components.NotekeaToolRailItem
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.rail_label
import app.notekea.resources.tree_files
import app.notekea.resources.tree_new_folder
import app.notekea.resources.tree_new_note
import app.notekea.resources.tree_search
import app.notekea.resources.tree_today
import app.notekea.resources.tree_untitled_folder
import app.notekea.resources.tree_untitled_note
import org.jetbrains.compose.resources.stringResource

/**
 * The vault's tool rail on the window's left edge (`ROADMAP.md` 1.11), there whether the sidebar is shown or not, as in
 * Android Studio: Files shows or hides the sidebar (selected while it is shown), then Search notes, New note, New folder
 * and Today, each with a tooltip naming it and its shortcut.
 */
@Composable
internal fun SidebarRail(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    NotekeaToolRail(
        items = railItems(state = state, onIntent = onIntent, onSearch = onSearch),
        label = stringResource(resource = Res.string.rail_label),
        modifier = modifier
    )
}

/** Files, Search notes, New note, New folder and Today, with their shortcuts. */
@Composable
private fun railItems(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    onSearch: () -> Unit
): List<NotekeaToolRailItem> {
    val conventions = state.platformConventions
    val shortcut = { it: Shortcut -> it.label(operatingSystem = conventions.operatingSystem) }
    val untitledNote = stringResource(resource = Res.string.tree_untitled_note)
    val untitledFolder = stringResource(resource = Res.string.tree_untitled_folder)
    return listOf(
        NotekeaToolRailItem(
            icon = NotekeaIcons.Files,
            label = stringResource(resource = Res.string.tree_files),
            onClick = { onIntent(VaultTreeIntent.OnSidebarToggle) },
            shortcutHint = shortcut(conventions.toggleSidebarShortcut),
            isSelected = !state.isSidebarCollapsed
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.Find,
            label = stringResource(resource = Res.string.tree_search),
            onClick = onSearch,
            shortcutHint = shortcut(conventions.quickSwitcherShortcut)
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.NewNote,
            label = stringResource(resource = Res.string.tree_new_note),
            onClick = { onIntent(VaultTreeIntent.OnNewNoteClick(baseName = untitledNote)) },
            shortcutHint = shortcut(conventions.newNoteShortcut)
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.NewFolder,
            label = stringResource(resource = Res.string.tree_new_folder),
            onClick = { onIntent(VaultTreeIntent.OnNewFolderClick(baseName = untitledFolder)) }
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.Today,
            label = stringResource(resource = Res.string.tree_today),
            onClick = { onIntent(VaultTreeIntent.OnTodayClick) },
            shortcutHint = shortcut(conventions.todayShortcut)
        )
    )
}

@Preview
@Composable
private fun SidebarRailPreview() {
    NotekeaPreviewThemes {
        SidebarRail(state = TreePreviewData.state(), onIntent = {}, onSearch = {})
    }
}

@Preview
@Composable
private fun SidebarRailCollapsedPreview() {
    NotekeaPreviewThemes {
        SidebarRail(state = TreePreviewData.state().copy(isSidebarCollapsed = true), onIntent = {}, onSearch = {})
    }
}

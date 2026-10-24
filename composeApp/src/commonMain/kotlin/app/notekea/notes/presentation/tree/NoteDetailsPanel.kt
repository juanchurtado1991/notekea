package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.RelativePath
import app.notekea.resources.Res
import app.notekea.resources.note_panel_aliases
import app.notekea.resources.note_panel_empty_message
import app.notekea.resources.note_panel_empty_title
import app.notekea.resources.note_panel_folder_notes
import app.notekea.resources.note_panel_tags
import app.notekea.resources.tree_show_sidebar
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val CONTENT_MAX_WIDTH_DP = 640
private const val LIST_SEPARATOR = ", "
private const val PREVIEW_HEIGHT_DP = 320
private const val PREVIEW_WIDTH_DP = 520
private const val TAG_PREFIX = "#"

/**
 * The main area beside the sidebar. Until the editor exists it shows what Notekea read from the selected note (its
 * title, folder, tags and aliases) or how many notes the selected folder holds, so the user can see the vault was
 * understood. It stays blank while the vault loads. When the sidebar is hidden, a button brings it back.
 */
@Composable
internal fun NoteDetailsPanel(
    state: VaultTreeState,
    onShowSidebar: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val selected = state.selectedPath
        val note = selected?.let { state.tree.notes[it] }
        when {
            state.isLoading -> Unit

            note != null -> NoteDetails(note = note)

            selected != null && selected in state.tree.folders -> FolderDetails(
                folder = selected,
                noteCount = state.tree.notes.keys
                    .count {
                        it.isWithin(folder = selected)
                    }
            )

            else -> EmptyState(
                title = stringResource(resource = Res.string.note_panel_empty_title),
                message = stringResource(resource = Res.string.note_panel_empty_message),
                modifier = Modifier.align(alignment = Alignment.Center)
            )
        }
        if (state.isSidebarCollapsed) {
            NotekeaIconButton(
                icon = NotekeaIcons.ShowSidebar,
                label = stringResource(resource = Res.string.tree_show_sidebar),
                onClick = onShowSidebar,
                modifier = Modifier.padding(all = NotekeaDimensions.SpacingS),
                shortcutHint = state.platformConventions.toggleSidebarShortcut.label(
                    operatingSystem = state.platformConventions.operatingSystem
                )
            )
        }
    }
}

@Composable
private fun NoteDetails(
    note: NoteEntry
) {
    val path = note.file.path
    DetailsColumn {
        Text(text = note.metadata.title ?: path.nameWithoutExtension, style = MaterialTheme.typography.headlineSmall)
        FolderLine(path = path)
        if (note.metadata.tags.isNotEmpty()) {
            DetailLine(
                label = stringResource(resource = Res.string.note_panel_tags),
                value = note.metadata.tags.joinToString(separator = " ") {
                    TAG_PREFIX +
                        it
                }
            )
        }
        if (note.metadata.aliases.isNotEmpty()) {
            DetailLine(
                label = stringResource(resource = Res.string.note_panel_aliases),
                value = note.metadata.aliases.joinToString(separator = LIST_SEPARATOR)
            )
        }
    }
}

@Composable
private fun FolderDetails(
    folder: RelativePath,
    noteCount: Int
) {
    DetailsColumn {
        Text(text = folder.name, style = MaterialTheme.typography.headlineSmall)
        FolderLine(path = folder)
        Text(
            text = pluralStringResource(Res.plurals.note_panel_folder_notes, noteCount, noteCount),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DetailsColumn(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(max = CONTENT_MAX_WIDTH_DP.dp)
            .padding(
                horizontal = NotekeaDimensions.SpacingXxl,
                vertical = NotekeaDimensions.SpacingXxl + NotekeaDimensions.SpacingL
            ),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) { content() }
}

@Composable
private fun FolderLine(
    path: RelativePath
) {
    path.parent?.takeUnless { it.isRoot }?.let {
        Text(
            text = it.value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String
) {
    Column(modifier = Modifier.padding(top = NotekeaDimensions.SpacingS)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NoteDetailsPanelPreviewFrame(
    state: VaultTreeState
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp).height(height = PREVIEW_HEIGHT_DP.dp)) {
            NoteDetailsPanel(state = state, onShowSidebar = {})
        }
    }
}

@Preview
@Composable
private fun NoteDetailsPanelPreview() {
    NoteDetailsPanelPreviewFrame(state = TreePreviewData.state())
}

@Preview
@Composable
private fun NoteDetailsPanelFolderPreview() {
    NoteDetailsPanelPreviewFrame(
        state = TreePreviewData.state(selected = RelativePath(value = "Projects")).copy(isSidebarCollapsed = true)
    )
}

@Preview
@Composable
private fun NoteDetailsPanelEmptyPreview() {
    NoteDetailsPanelPreviewFrame(state = TreePreviewData.state(selected = null))
}

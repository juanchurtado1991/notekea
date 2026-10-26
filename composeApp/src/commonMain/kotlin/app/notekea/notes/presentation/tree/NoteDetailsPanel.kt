package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.RelativePath
import app.notekea.resources.Res
import app.notekea.resources.note_panel_empty_message
import app.notekea.resources.note_panel_empty_title
import app.notekea.resources.note_panel_folder_notes
import app.notekea.resources.tree_show_sidebar
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val CONTENT_MAX_WIDTH_DP = 640
private const val PREVIEW_HEIGHT_DP = 320
private const val PREVIEW_WIDTH_DP = 520

/**
 * The main area beside the sidebar: the selected note in [noteContent] (the editor), the selected file that is not a
 * note ([FileDetails]), how many notes the selected folder holds, or a hint when nothing is selected. It stays blank while the vault loads. When the sidebar is hidden,
 * a button brings it back.
 */
@Composable
internal fun NoteDetailsPanel(
    state: VaultTreeState,
    onShowSidebar: () -> Unit,
    noteContent: @Composable (NoteEntry) -> Unit,
    onIntent: (VaultTreeIntent) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SelectedContent(state = state, noteContent = noteContent, onIntent = onIntent)
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

/** What the selection shows: a note in the editor, a file, a folder's note count, or a hint when nothing is selected. */
@Composable
private fun BoxScope.SelectedContent(
    state: VaultTreeState,
    noteContent: @Composable (NoteEntry) -> Unit,
    onIntent: (VaultTreeIntent) -> Unit
) {
    val selected = state.selectedPath
    // A selected note that is gone (deleted or moved outside Notekea) still reaches the editor, which explains it.
    val note = selected?.let { state.tree.notes[it] ?: missingNote(path = it) }
    val filePreview = state.filePreview?.takeIf { it.file.path == selected }
    when {
        state.isLoading -> Unit

        note != null -> noteContent(note)

        filePreview != null -> FileDetails(
            preview = filePreview,
            platformConventions = state.platformConventions,
            onIntent = onIntent
        )

        selected != null && selected in state.tree.attachments -> Unit

        selected != null && selected in state.tree.folders -> FolderDetails(
            folder = selected,
            fileCount = (state.tree.notes.keys + state.tree.attachments.keys)
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
}

private fun missingNote(
    path: RelativePath
): NoteEntry? = if (path.isMarkdown) {
    NoteEntry(file = NoteFile(path = path, size = null, modifiedAt = null), metadata = NoteMetadata.NONE)
} else {
    null
}

@Composable
private fun FolderDetails(
    folder: RelativePath,
    fileCount: Int
) {
    DetailsColumn {
        Text(text = folder.name, style = MaterialTheme.typography.headlineSmall)
        FolderLine(path = folder)
        Text(
            text = pluralStringResource(Res.plurals.note_panel_folder_notes, fileCount, fileCount),
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
private fun NoteDetailsPanelPreviewFrame(
    state: VaultTreeState
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp).height(height = PREVIEW_HEIGHT_DP.dp)) {
            NoteDetailsPanel(state = state, onShowSidebar = {}, noteContent = { PreviewNoteContent(note = it) })
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

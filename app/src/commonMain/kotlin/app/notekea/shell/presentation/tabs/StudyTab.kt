package app.notekea.shell.presentation.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.cards.presentation.StudyContent
import app.notekea.cards.presentation.StudyEffect
import app.notekea.cards.presentation.StudyIntent
import app.notekea.cards.presentation.StudyState
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.shell.domain.Workspace
import app.notekea.shell.presentation.workspace.WorkspaceIntent
import app.notekea.shell.presentation.workspace.WorkspaceViewModels
import app.notekea.tree.presentation.model.VaultTreeState

/**
 * What the Study tab follows while the vault is open (4.6): its notes and whether the tab shows, as it reads the cards
 * only then. Open the note opens the card's note; a card scheduled in a note reaches the editor that has it open.
 */
@Composable
internal fun FollowStudy(
    viewModels: WorkspaceViewModels,
    state: VaultTreeState
) {
    val study = viewModels.study

    val tabs by viewModels
        .workspace
        .state
        .collectAsState()

    val isShown = tabs
        .workspace
        .panes
        .any {
            it.activePath == Workspace.STUDY
        }

    LaunchedEffect(study, state.tree.notes) {
        study.onIntent(intent = StudyIntent.OnNotesChange(notes = state.tree.notes.values))
    }

    LaunchedEffect(study, isShown) {
        study.onIntent(intent = StudyIntent.OnShownChange(isShown = isShown))
    }

    LaunchedEffect(study) {
        study.effects.collect { effect ->
            when (effect) {
                is StudyEffect.OpenNote -> open(
                    viewModels = viewModels,
                    path = effect.path
                )

                is StudyEffect.NoteRewritten -> viewModels.editors.forEach { editor ->
                    editor.onIntent(intent = EditorIntent.OnNoteRewritten(path = effect.path))
                }
            }
        }
    }
}

private fun open(
    viewModels: WorkspaceViewModels,
    path: app.notekea.core.notes.domain.RelativePath
) = viewModels.workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = path))

@Preview
@Composable
private fun FollowStudyPreview() {
    NotekeaPreviewThemes {
        StudyContent(
            state = StudyState(isLoading = false),
            onIntent = {}
        )
    }
}

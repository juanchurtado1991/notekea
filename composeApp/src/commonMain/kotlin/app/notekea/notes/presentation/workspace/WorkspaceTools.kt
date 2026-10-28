package app.notekea.notes.presentation.workspace

import app.notekea.notes.domain.WorkspaceStore
import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.AttachmentStore
import app.notekea.notes.domain.GetNoteCoverUseCase
import app.notekea.notes.domain.GetSidePanelUseCase
import app.notekea.notes.domain.GetWorkspaceUseCase
import app.notekea.notes.domain.SaveSidePanelUseCase

/** What the tabs need (`ROADMAP.md` 1.11), given together so the ViewModel's constructor stays short. */
class WorkspaceTools(
    val getWorkspace: GetWorkspaceUseCase,
    val workspaceStore: WorkspaceStore,
    val getSidePanel: GetSidePanelUseCase,
    val saveSidePanel: SaveSidePanelUseCase,
    val getNoteCover: GetNoteCoverUseCase,
    val attachmentStore: AttachmentStore,
    val dispatchers: AppDispatchers
)

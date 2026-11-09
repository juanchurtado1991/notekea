package app.notekea.shell.presentation

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.domain.AttachmentStore
import app.notekea.shell.domain.GetNoteCoverUseCase
import app.notekea.shell.domain.GetSidePanelUseCase
import app.notekea.shell.domain.GetWorkspaceUseCase
import app.notekea.shell.domain.SaveSidePanelUseCase
import app.notekea.shell.domain.WorkspaceStore

/** What the tabs need, given together so the ViewModel's constructor stays short. */
class WorkspaceTools(
    val getWorkspace: GetWorkspaceUseCase,
    val workspaceStore: WorkspaceStore,
    val getSidePanel: GetSidePanelUseCase,
    val saveSidePanel: SaveSidePanelUseCase,
    val getNoteCover: GetNoteCoverUseCase,
    val attachmentStore: AttachmentStore,
    val dispatchers: AppDispatchers
)

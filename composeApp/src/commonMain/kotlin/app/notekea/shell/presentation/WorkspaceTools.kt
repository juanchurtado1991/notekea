package app.notekea.shell.presentation

import app.notekea.shell.domain.WorkspaceStore
import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.AttachmentStore
import app.notekea.shell.domain.GetNoteCoverUseCase
import app.notekea.shell.domain.GetSidePanelUseCase
import app.notekea.shell.domain.GetWorkspaceUseCase
import app.notekea.shell.domain.SaveSidePanelUseCase

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

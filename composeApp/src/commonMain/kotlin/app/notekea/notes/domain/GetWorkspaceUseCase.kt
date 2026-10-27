package app.notekea.notes.domain

/** The tabs and panes the vault was left with, or a single empty pane the first time. */
class GetWorkspaceUseCase(private val workspaceStore: WorkspaceStore) {
    suspend operator fun invoke(
        vaultPath: String
    ): Workspace = workspaceStore.read(vaultPath = vaultPath) ?: Workspace.EMPTY
}

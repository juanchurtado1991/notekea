package app.notekea.notes.domain

/** Remembers the vault's tabs and panes, so they are back when it opens again. */
class SaveWorkspaceUseCase(private val workspaceStore: WorkspaceStore) {
    suspend operator fun invoke(
        vaultPath: String,
        workspace: Workspace
    ) = workspaceStore.write(vaultPath = vaultPath, workspace = workspace)
}

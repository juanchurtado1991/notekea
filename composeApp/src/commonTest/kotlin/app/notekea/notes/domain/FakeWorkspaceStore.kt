package app.notekea.notes.domain

/** Each vault's tabs in memory. */
class FakeWorkspaceStore : WorkspaceStore {
    val workspaces = mutableMapOf<String, Workspace>()

    override suspend fun read(
        vaultPath: String
    ): Workspace? = workspaces[vaultPath]

    override suspend fun write(
        vaultPath: String,
        workspace: Workspace
    ) {
        workspaces[vaultPath] = workspace
    }
}

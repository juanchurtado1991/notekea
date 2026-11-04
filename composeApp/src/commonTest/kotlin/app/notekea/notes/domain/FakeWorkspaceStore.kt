package app.notekea.notes.domain

/** Each vault's tabs in memory; every call goes through [gate], as the disk takes its time. */
class FakeWorkspaceStore : WorkspaceStore {
    val workspaces = mutableMapOf<String, Workspace>()
    val gate = Gate()

    override suspend fun read(
        vaultPath: String
    ): Workspace? {
        gate.pass()
        return workspaces[vaultPath]
    }

    override suspend fun write(
        vaultPath: String,
        workspace: Workspace
    ) {
        gate.pass()
        workspaces[vaultPath] = workspace
    }
}

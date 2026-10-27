package app.notekea.notes.domain

/** Keeps each vault's open tabs and panes between runs (`ROADMAP.md` 1.11). */
interface WorkspaceStore {
    /** The tabs the vault was left with, or null when it has none saved. */
    suspend fun read(
        vaultPath: String
    ): Workspace?

    suspend fun write(
        vaultPath: String,
        workspace: Workspace
    )
}

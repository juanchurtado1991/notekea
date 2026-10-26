package app.notekea.notes.domain

/** Obsidian's settings of a vault (`.obsidian/app.json`) that Notekea follows. */
interface VaultSettingsReader {
    /** The `attachmentFolderPath` setting, or null when the vault has none or it cannot be read. */
    suspend fun attachmentFolderPath(
        vaultPath: String
    ): String?
}

package app.notekea.notes.domain

/** Obsidian's settings of a vault (in `.obsidian`) that Notekea follows. */
interface VaultSettingsReader {
    /** The `attachmentFolderPath` setting, or null when the vault has none or it cannot be read. */
    suspend fun attachmentFolderPath(
        vaultPath: String
    ): String?

    /**
     * The Daily notes plugin's settings (`.obsidian/daily-notes.json`): blank fields as Obsidian reads them (the vault
     * root, `YYYY-MM-DD`, no template), and null when the vault has no such file or it cannot be read.
     */
    suspend fun dailyNotes(
        vaultPath: String
    ): DailyNoteSettings?
}

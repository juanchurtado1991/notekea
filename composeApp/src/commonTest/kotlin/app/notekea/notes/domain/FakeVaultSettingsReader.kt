package app.notekea.notes.domain

/** Obsidian's vault settings in memory: none unless a test sets them. */
class FakeVaultSettingsReader(
    var attachmentFolder: String? = null,
    var dailyNotes: DailyNoteSettings? = null
) : VaultSettingsReader {
    override suspend fun attachmentFolderPath(
        vaultPath: String
    ): String? = attachmentFolder

    override suspend fun dailyNotes(
        vaultPath: String
    ): DailyNoteSettings? = dailyNotes
}

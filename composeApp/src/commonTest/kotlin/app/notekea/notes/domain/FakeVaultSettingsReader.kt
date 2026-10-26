package app.notekea.notes.domain

/** Obsidian's settings as a test sets them: [attachmentFolder] is what `app.json` says, null when it says nothing. */
class FakeVaultSettingsReader(var attachmentFolder: String? = null) : VaultSettingsReader {
    override suspend fun attachmentFolderPath(
        vaultPath: String
    ): String? = attachmentFolder
}

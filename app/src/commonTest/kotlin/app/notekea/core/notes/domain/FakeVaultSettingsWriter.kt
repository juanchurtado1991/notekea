package app.notekea.core.notes.domain

import app.notekea.core.domain.Outcome

/** [VaultSettingsWriter] in memory: what was written, by file, and a failure to give instead when [fails]. */
class FakeVaultSettingsWriter(var fails: Boolean = false) : VaultSettingsWriter {
    val written = mutableListOf<String>()

    override suspend fun setAttachmentFolder(
        vaultPath: String,
        folder: String
    ): Outcome<Unit, NotesError> = written(text = "app.json attachmentFolderPath=$folder")

    override suspend fun setDailyNotes(
        vaultPath: String,
        folder: String,
        format: String
    ): Outcome<Unit, NotesError> = written(text = "daily-notes.json folder=$folder format=$format")

    override suspend fun setTemplatesFolder(
        vaultPath: String,
        folder: String
    ): Outcome<Unit, NotesError> = written(text = "templates.json folder=$folder")

    private fun written(text: String): Outcome<Unit, NotesError> {
        if (fails) return Outcome.Failure(error = NotesError.OperationFailed(path = ".obsidian"))
        written += text

        return Outcome.Success(value = Unit)
    }
}

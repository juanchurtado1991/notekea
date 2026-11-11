package app.notekea.core.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.VaultSettingsWriter
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * [VaultSettingsWriter] on Obsidian's files in `.obsidian/`, each field set in place with [JsonFields] so the rest of
 * the file stays as Obsidian wrote it; a missing file or folder is made.
 */
class ObsidianVaultSettingsWriter(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers
) : VaultSettingsWriter {
    override suspend fun setAttachmentFolder(
        vaultPath: String,
        folder: String
    ): Outcome<Unit, NotesError> = write(
        vaultPath = vaultPath,
        fileName = APP_FILE,
        fields = mapOf(ATTACHMENTS to folder)
    )

    override suspend fun setDailyNotes(
        vaultPath: String,
        folder: String,
        format: String
    ): Outcome<Unit, NotesError> = write(
        vaultPath = vaultPath,
        fileName = DAILY_NOTES_FILE,
        fields = mapOf(FOLDER to folder, FORMAT to format)
    )

    override suspend fun setTemplatesFolder(
        vaultPath: String,
        folder: String
    ): Outcome<Unit, NotesError> = write(
        vaultPath = vaultPath,
        fileName = TEMPLATES_FILE,
        fields = mapOf(FOLDER to folder)
    )

    private suspend fun write(
        vaultPath: String,
        fileName: String,
        fields: Map<String, String>
    ): Outcome<Unit, NotesError> = withContext(context = dispatchers.io) {
        val folder: Path = vaultPath.toPath() / SETTINGS_FOLDER
        val file = folder / fileName

        try {
            fileSystem.createDirectories(dir = folder)

            val before = if (fileSystem.exists(path = file)) {
                fileSystem
                    .read(file = file) { readUtf8() }
            } else {
                ""
            }

            val after = fields
                .entries
                .fold(initial = before) { json, (name, value) ->
                    JsonFields.withText(
                        json = json,
                        name = name,
                        value = value
                    )
                }

            fileSystem.write(file = file) { writeUtf8(string = after) }
            Outcome.Success(value = Unit)
        } catch (_: IOException) {
            Outcome.Failure(error = NotesError.OperationFailed(path = "$SETTINGS_FOLDER/$fileName"))
        }
    }

    private companion object {
        const val SETTINGS_FOLDER = ".obsidian"
        const val APP_FILE = "app.json"
        const val DAILY_NOTES_FILE = "daily-notes.json"
        const val TEMPLATES_FILE = "templates.json"
        const val ATTACHMENTS = "attachmentFolderPath"
        const val FOLDER = "folder"
        const val FORMAT = "format"
    }
}

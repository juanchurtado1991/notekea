package app.notekea.core.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.VaultSettingsReader
import app.notekea.core.notes.domain.templates.DailyNoteSettings
import app.notekea.core.notes.domain.templates.TemplateSettings
import co.touchlab.kermit.Logger
import com.ghost.serialization.Ghost
import com.ghost.serialization.exception.GhostJsonException
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath

/**
 * [VaultSettingsReader] on Obsidian's `.obsidian/app.json`, `.obsidian/daily-notes.json` and
 * `.obsidian/templates.json`, decoded with Ghost. A vault without a file, or with one that cannot be read, simply has
 * no setting: Notekea falls back to its defaults.
 */
class ObsidianVaultSettingsReader(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers
) : VaultSettingsReader {
    private val logger = Logger.withTag(tag = LOG_TAG)

    override suspend fun attachmentFolderPath(vaultPath: String): String? = readJson(
        vaultPath = vaultPath,
        fileName = SETTINGS_FILE
    ) { json -> Ghost.deserialize<ObsidianAppSettingsDto>(json).attachmentFolderPath }

    override suspend fun dailyNotes(vaultPath: String): DailyNoteSettings? = readJson(
        vaultPath = vaultPath,
        fileName = DAILY_NOTES_FILE
    ) { json ->
        val dto = Ghost.deserialize<ObsidianDailyNotesDto>(json)

        DailyNoteSettings(
            folder = dto
                .folder
                ?.trim()
                .orEmpty()
                .trim(RelativePath.SEPARATOR.single()),
            format = dto
                .format
                ?.trim()
                .orEmpty()
                .ifEmpty { MomentFormat.ISO_DATE },
            template = dto
                .template
                ?.trim()
                ?.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
                ?.ifEmpty { null }
        )
    }

    override suspend fun templates(vaultPath: String): TemplateSettings? = readJson(
        vaultPath = vaultPath,
        fileName = TEMPLATES_FILE
    ) { json ->
        val dto = Ghost.deserialize<ObsidianTemplatesDto>(json)

        TemplateSettings(
            folder = dto
                .folder
                ?.trim()
                .orEmpty()
                .trim(RelativePath.SEPARATOR.single()),
            dateFormat = dto
                .dateFormat
                ?.trim()
                .orEmpty()
                .ifEmpty { MomentFormat.ISO_DATE },
            timeFormat = dto
                .timeFormat
                ?.trim()
                .orEmpty()
                .ifEmpty { MomentFormat.SHORT_TIME }
        )
    }

    /**
     * [decode]s the settings file [fileName] of the vault's `.obsidian` folder; null when it is missing or unreadable.
     */
    private suspend fun <T> readJson(
        vaultPath: String,
        fileName: String,
        decode: (String) -> T
    ): T? = withContext(context = dispatchers.io) {
        val file = vaultPath.toPath() / SETTINGS_FOLDER / fileName

        try {
            if (!fileSystem.exists(path = file)) return@withContext null

            decode(fileSystem.read(file = file) { readUtf8() })
        } catch (exception: IOException) {
            unreadable(
                fileName = fileName,
                cause = exception
            )
        } catch (exception: GhostJsonException) {
            unreadable(
                fileName = fileName,
                cause = exception
            )
        }
    }

    private fun <T> unreadable(
        fileName: String,
        cause: Exception
    ): T? {
        logger
            .w(throwable = cause) {
                UNREADABLE_MESSAGE + fileName
            }

        return null
    }

    companion object {
        private const val LOG_TAG = "VaultSettingsReader"
        private const val SETTINGS_FILE = "app.json"
        private const val SETTINGS_FOLDER = ".obsidian"
        private const val DAILY_NOTES_FILE = "daily-notes.json"
        private const val TEMPLATES_FILE = "templates.json"
        private const val UNREADABLE_MESSAGE =
            "A vault setting cannot be read, using the default instead: "
    }
}

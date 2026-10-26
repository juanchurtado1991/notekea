package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.VaultSettingsReader
import co.touchlab.kermit.Logger
import com.ghost.serialization.Ghost
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * [VaultSettingsReader] on Obsidian's `.obsidian/app.json`, decoded with Ghost. A vault without the file, or with one
 * that cannot be read, simply has no setting: Notekea falls back to its defaults.
 */
class ObsidianVaultSettingsReader(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers
) : VaultSettingsReader {
    private val logger = Logger.withTag(tag = LOG_TAG)

    override suspend fun attachmentFolderPath(
        vaultPath: String
    ): String? = withContext(context = dispatchers.io) {
        val file = vaultPath.toPath() / SETTINGS_FOLDER / SETTINGS_FILE
        try {
            if (!fileSystem.exists(path = file)) return@withContext null
            val json = fileSystem.read(file = file) { readUtf8() }
            Ghost.deserialize<ObsidianAppSettingsDto>(json).attachmentFolderPath
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            logger.w(throwable = exception) { UNREADABLE_MESSAGE }
            null
        }
    }

    companion object {
        private const val LOG_TAG = "VaultSettingsReader"
        private const val SETTINGS_FILE = "app.json"
        private const val SETTINGS_FOLDER = ".obsidian"
        private const val UNREADABLE_MESSAGE = "app.json cannot be read; using the default attachments folder"
    }
}

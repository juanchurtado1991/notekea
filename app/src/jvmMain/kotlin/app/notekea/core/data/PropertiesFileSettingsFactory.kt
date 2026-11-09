package app.notekea.core.data

import co.touchlab.kermit.Logger
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Properties

/**
 * App settings as `settings.properties` in the app data directory (`PLATFORM_GUIDELINES.md`). Each change is written to
 * a temporary file of its own and moved into place, so a crash never leaves a half-written file and two processes never
 * move one away from the other; saves run one at a time. An unreadable file is logged and replaced by empty settings.
 */
class PropertiesFileSettingsFactory(private val appDataDirectory: String) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    fun create(): Settings {
        val file = Path.of(appDataDirectory, SETTINGS_FILE_NAME)

        return PropertiesSettings(
            delegate = load(file = file),
            onModify = {
                save(
                    properties = it,
                    file = file
                )
            }
        )
    }

    private fun load(file: Path): Properties {
        val properties = Properties()
        if (!Files.exists(file)) return properties

        try {
            Files.newInputStream(file).use { properties.load(it) }
        } catch (exception: IOException) {
            logger.w(throwable = exception) { UNREADABLE_MESSAGE }
        } catch (exception: IllegalArgumentException) {
            logger.w(throwable = exception) { UNREADABLE_MESSAGE }
        }

        return properties
    }

    private fun moveIntoPlace(
        source: Path,
        target: Path
    ) {
        try {
            Files.move(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun save(
        properties: Properties,
        file: Path
    ) = synchronized(lock = SAVING) {
        Files.createDirectories(file.parent)
        val temporaryFile = Files.createTempFile(file.parent, SETTINGS_FILE_NAME, TEMPORARY_SUFFIX)

        try {
            Files.newOutputStream(temporaryFile).use { properties.store(it, null) }

            moveIntoPlace(
                source = temporaryFile,
                target = file
            )
        } finally {
            Files.deleteIfExists(temporaryFile)
        }
    }

    companion object {
        private val SAVING = Any()
        private const val LOG_TAG = "PropertiesFileSettings"
        private const val SETTINGS_FILE_NAME = "settings.properties"
        private const val TEMPORARY_SUFFIX = ".tmp"
        private const val UNREADABLE_MESSAGE =
            "Settings file is unreadable; starting with empty settings"
    }
}

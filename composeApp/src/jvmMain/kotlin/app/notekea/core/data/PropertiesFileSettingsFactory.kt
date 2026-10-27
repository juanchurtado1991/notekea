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
 * App settings as `settings.properties` in the app data directory (`PLATFORM_GUIDELINES.md`). The JVM default,
 * `java.util.prefs`, would write outside it. Each change is written to a temporary file and moved into place, so a
 * crash never leaves a half-written file; an unreadable file is logged and replaced by empty settings. Saves run one at
 * a time: features save from several threads at once (the tree's open folders and the tabs, on one click), and two
 * saves sharing the temporary file would move it away from each other.
 */
class PropertiesFileSettingsFactory(private val appDataDirectory: String) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val saving = Any()

    fun create(): Settings {
        val file = Path.of(appDataDirectory, SETTINGS_FILE_NAME)
        return PropertiesSettings(
            delegate = load(file = file),
            onModify = { save(properties = it, file = file) }
        )
    }

    private fun load(
        file: Path
    ): Properties {
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
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun save(
        properties: Properties,
        file: Path
    ) = synchronized(lock = saving) {
        Files.createDirectories(file.parent)
        val temporaryFile = file.resolveSibling(SETTINGS_FILE_NAME + TEMPORARY_SUFFIX)
        Files.newOutputStream(temporaryFile).use { properties.store(it, null) }
        moveIntoPlace(source = temporaryFile, target = file)
    }

    companion object {
        private const val LOG_TAG = "PropertiesFileSettings"
        private const val SETTINGS_FILE_NAME = "settings.properties"
        private const val TEMPORARY_SUFFIX = ".tmp"
        private const val UNREADABLE_MESSAGE = "Settings file is unreadable; starting with empty settings"
    }
}

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

    // The settings as this Notekea last read or wrote them: what differs from it is what it changed.
    private val written = Properties()

    fun create(): Settings {
        val file = Path.of(appDataDirectory, SETTINGS_FILE_NAME)

        val loaded = load(file = file)
        written.putAll(loaded)

        return PropertiesSettings(
            delegate = loaded,
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

        // Another Notekea running writes the same file: what this one changed since it last wrote goes over what
        // is on disk now, never its whole memory, which would undo the other's settings (2026-11-13).
        val merged = load(file = file)

        // One picture of the settings for the whole save: a key another thread adds after its names were read
        // would be taken as written without having been (one key of 400 was lost, the full check of 2026-11-14).
        val saved = properties.clone() as Properties

        (saved.stringPropertyNames() + written.stringPropertyNames()).forEach { key ->
            val now = saved.getProperty(key)
            val isChanged = now != written.getProperty(key)

            when {
                !isChanged -> Unit
                now == null -> merged.remove(key)
                else -> merged.setProperty(key, now)
            }
        }

        written.clear()
        written.putAll(saved)

        try {
            Files.newOutputStream(temporaryFile).use { merged.store(it, null) }

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

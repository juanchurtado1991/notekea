package app.notekea.core.data

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class PropertiesFileSettingsFactoryTest {
    private val root: Path = createTempDirectory(prefix = TEMP_PREFIX)
    private val appData: Path = root.resolve(APP_DATA_FOLDER)

    @AfterTest
    fun tearDown() {
        root.deleteRecursively()
    }

    @Test
    fun `settings are saved in the app data directory and survive a restart`() {
        PropertiesFileSettingsFactory(
            appDataDirectory = appData.toString()
        ).create().putString(key = KEY, value = VALUE)

        val reloaded = PropertiesFileSettingsFactory(appDataDirectory = appData.toString()).create()

        assertTrue(actual = appData.resolve(SETTINGS_FILE).exists())
        assertEquals(
            expected = VALUE,
            actual = reloaded.getStringOrNull(key = KEY)
        )
        assertEquals(
            expected = listOf(SETTINGS_FILE),
            actual = Files.list(appData).use { files -> files.map { it.fileName.toString() }.toList() }
        )
    }

    @Test
    fun `an unreadable settings file starts empty instead of failing`() {
        Files.createDirectories(appData)
        appData.resolve(SETTINGS_FILE).writeText(text = MALFORMED)

        val settings = PropertiesFileSettingsFactory(appDataDirectory = appData.toString()).create()

        assertEquals(
            expected = emptySet(),
            actual = settings.keys
        )
    }

    companion object {
        private const val APP_DATA_FOLDER = "notekea"
        private const val KEY = "vaults.recent.count"
        private const val MALFORMED = "broken=\\u12"
        private const val SETTINGS_FILE = "settings.properties"
        private const val TEMP_PREFIX = "notekea-settings"
        private const val VALUE = "1"
    }
}

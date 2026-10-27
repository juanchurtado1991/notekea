package app.notekea.core.data

import java.nio.file.Files
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
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

    @Test
    fun `settings changed from several threads at once are all saved, none failing`() {
        // 1.11 smoke: opening a folder saved the tree's open folders and the tabs at the same time, and one save moved
        // the temporary file away while the other was still writing it ("Something went wrong").
        val settings = PropertiesFileSettingsFactory(appDataDirectory = appData.toString()).create()
        val pool = Executors.newFixedThreadPool(THREADS)
        val failures = ConcurrentLinkedQueue<Throwable>()
        val writes = (0 until THREADS).map { thread ->
            pool.submit {
                repeat(times = WRITES) { write ->
                    try {
                        settings.putString(key = "key.$thread.$write", value = VALUE)
                    } catch (exception: Exception) {
                        failures += exception
                    }
                }
            }
        }
        writes.forEach { it.get() }
        pool.shutdown()

        val reloaded = PropertiesFileSettingsFactory(appDataDirectory = appData.toString()).create()
        assertEquals(
            expected = emptyList<Throwable>() to THREADS * WRITES,
            actual = failures.toList() to reloaded.keys.size
        )
    }

    companion object {
        private const val THREADS = 8
        private const val WRITES = 50
        private const val APP_DATA_FOLDER = "notekea"
        private const val KEY = "vaults.recent.count"
        private const val MALFORMED = "broken=\\u12"
        private const val SETTINGS_FILE = "settings.properties"
        private const val TEMP_PREFIX = "notekea-settings"
        private const val VALUE = "1"
    }
}

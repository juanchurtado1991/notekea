package app.notekea.notes.data

import app.notekea.core.data.PropertiesFileSettingsFactory
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.Bookmarks
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.listDirectoryEntries
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A vault's bookmarks on the real settings file (`ROADMAP.md` 1.13): written by one run and read back by the next, in
 * order, paths with spaces, accents and folders included, and only the app's data folder written.
 */
@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class BookmarksOnDiskTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    private val appData = createTempDirectory(prefix = TEMP_PREFIX)

    @AfterTest
    fun tearDown() {
        appData.deleteRecursively()
    }

    @Test
    fun `the bookmarks of one run are back in the next, in their order`() = runTest(context = testDispatcher) {
        val bookmarks = Bookmarks(
            paths = listOf(
                "Recipes/Café con leche.md",
                "Projects",
                "attachments/Arepas plate.png",
                "Notes with spaces.md"
            )
                .map { RelativePath(value = it) }
        )
        store().write(vaultPath = VAULT, bookmarks = bookmarks)

        assertEquals(
            expected = bookmarks to listOf("settings.properties"),
            actual = store().read(vaultPath = VAULT) to appData.listDirectoryEntries().map { it.fileName.toString() }
        )
    }

    /** A store over the settings file as a new run of the app opens it. */
    private fun store(): SettingsBookmarkStore = SettingsBookmarkStore(
        settings = PropertiesFileSettingsFactory(appDataDirectory = appData.toString()).create(),
        dispatchers = dispatchers
    )

    companion object {
        private const val TEMP_PREFIX = "notekea-bookmarks"
        private const val VAULT = "/home/ana/Personal Notes"
        private val PATHS =
            listOf("Recipes/Café con leche.md", "Projects", "attachments/Arepas plate.png", "Notes with spaces.md")
    }
}

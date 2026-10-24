package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TreePreferences
import app.notekea.notes.domain.TreeSort
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsTreePreferencesStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val settings = MapSettings()
    private val store =
        SettingsTreePreferencesStore(settings = settings, dispatchers = FakeAppDispatchers(dispatcher = testDispatcher))

    @Test
    fun `each vault remembers its own expanded folders and the sort is shared`() = runTest(context = testDispatcher) {
        val notes = TreePreferences(
            expandedFolders = setOf(path("Projects"), path("Projects/Garden")),
            sort = TreeSort.MODIFIED
        )
        store.write(vaultPath = NOTES, preferences = notes)
        store.write(
            vaultPath = WORK,
            preferences = TreePreferences(expandedFolders = setOf(path("Clients")), sort = TreeSort.MODIFIED)
        )

        assertEquals(
            expected = listOf(
                notes,
                TreePreferences(expandedFolders = setOf(path("Clients")), sort = TreeSort.MODIFIED)
            ),
            actual = listOf(store.read(vaultPath = NOTES), store.read(vaultPath = WORK))
        )
    }

    @Test
    fun `a vault never seen starts collapsed and sorted by name`() = runTest(context = testDispatcher) {
        assertEquals(
            expected = TreePreferences.DEFAULT,
            actual = store.read(vaultPath = NOTES)
        )
    }

    @Test
    fun `collapsing everything removes the key and an unknown sort falls back to name`() = runTest(
        context = testDispatcher
    ) {
        store.write(
            vaultPath = NOTES,
            preferences = TreePreferences(expandedFolders = setOf(path("Projects")), sort = TreeSort.NAME)
        )
        store.write(vaultPath = NOTES, preferences = TreePreferences.DEFAULT)
        settings.putString(key = "tree.sort", value = "SIZE")

        assertEquals(
            expected = setOf("tree.sort", "tree.sidebar.width", "tree.sidebar.collapsed"),
            actual = settings.keys
        )
        assertEquals(
            expected = TreePreferences.DEFAULT,
            actual = store.read(vaultPath = NOTES)
        )
    }

    @Test
    fun `the vault key is a stable FNV-1a hash of its path`() = runTest(context = testDispatcher) {
        store.write(
            vaultPath = "a",
            preferences = TreePreferences(expandedFolders = setOf(path("x")), sort = TreeSort.NAME)
        )

        assertEquals(
            expected = setOf(
                "tree.af63dc4c8601ec8c.expanded",
                "tree.sort",
                "tree.sidebar.width",
                "tree.sidebar.collapsed"
            ),
            actual = settings.keys
        )
    }

    @Test
    fun `the sidebar width and visibility are kept, and a width out of bounds is clamped`() = runTest(
        context = testDispatcher
    ) {
        store.write(
            vaultPath = NOTES,
            preferences = TreePreferences.DEFAULT.copy(sidebarWidthDp = 320, isSidebarCollapsed = true)
        )
        val kept = store.read(vaultPath = WORK)
        settings.putInt(key = "tree.sidebar.width", value = 5_000)

        assertEquals(
            expected = listOf(320 to true, TreePreferences.MAX_SIDEBAR_WIDTH_DP to true),
            actual = listOf(kept, store.read(vaultPath = NOTES)).map { it.sidebarWidthDp to it.isSidebarCollapsed }
        )
    }

    private fun path(
        value: String
    ): RelativePath = RelativePath(value = value)

    companion object {
        private const val NOTES = "/home/ana/Notes"
        private const val WORK = "/home/ana/Work"
    }
}

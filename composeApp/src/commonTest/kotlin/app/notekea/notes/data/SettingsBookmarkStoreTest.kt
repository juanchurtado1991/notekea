package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.Bookmarks
import app.notekea.notes.domain.RelativePath
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Each vault's bookmarks kept in the app settings (`ROADMAP.md` 1.13), in order, and gone when none are left. */
class SettingsBookmarkStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val settings = MapSettings()
    private val store =
        SettingsBookmarkStore(settings = settings, dispatchers = FakeAppDispatchers(dispatcher = testDispatcher))

    @Test
    fun `each vault keeps its own bookmarks in order, names with spaces and accents included`() =
        runTest(context = testDispatcher) {
            val notes =
                Bookmarks(paths = listOf("Recipes/Café con leche.md", "Welcome.md", "Recipes").map(::RelativePath))
            store.write(vaultPath = NOTES, bookmarks = notes)
            store.write(vaultPath = WORK, bookmarks = Bookmarks(paths = listOf(RelativePath(value = "Clients"))))

            assertEquals(
                expected = listOf(notes, Bookmarks(paths = listOf(RelativePath(value = "Clients"))), Bookmarks()),
                actual = listOf(
                    store.read(vaultPath = NOTES),
                    store.read(vaultPath = WORK),
                    store.read(vaultPath = "/new")
                )
            )
        }

    @Test
    fun `no bookmarks left removes the vault's key`() = runTest(context = testDispatcher) {
        store.write(vaultPath = NOTES, bookmarks = Bookmarks(paths = listOf(RelativePath(value = "Welcome.md"))))
        store.write(vaultPath = NOTES, bookmarks = Bookmarks())

        assertEquals(expected = emptySet(), actual = settings.keys)
    }

    companion object {
        private const val NEW_VAULT = "/home/ana/New"
        private const val NOTES = "/home/ana/Notes"
        private const val WORK = "/home/ana/Work"
    }
}

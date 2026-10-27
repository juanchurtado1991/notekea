package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.EditorPane
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.Workspace
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsWorkspaceStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val settings = MapSettings()
    private val store = SettingsWorkspaceStore(
        settings = settings,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    @Test
    fun `each vault's panes and tabs come back as they were left, and a vault never saved has none`() =
        runTest(context = testDispatcher) {
            val split = Workspace(
                panes = listOf(
                    EditorPane(
                        tabs = listOf("Recipes/Arepas.md", "attachments/plate.png").map(::RelativePath),
                        active = 1
                    ),
                    EditorPane(tabs = listOf(RelativePath(value = "Projects")), active = 0)
                ),
                activePane = 1
            )

            store.write(vaultPath = "/home/ana/Notes", workspace = split)
            store.write(vaultPath = "/home/ana/Work", workspace = Workspace.EMPTY)

            assertEquals(
                expected = listOf(split, Workspace.EMPTY, null),
                actual = listOf("/home/ana/Notes", "/home/ana/Work", "/home/ana/Other").map {
                    store.read(vaultPath = it)
                }
            )
        }

    @Test
    fun `text that does not make a workspace is no workspace`() = runTest(context = testDispatcher) {
        val key = "workspace." + VaultKeyHash.of(vaultPath = "/v")
        val broken = listOf("x\n/0\nA.md", "0\nA.md", "3\n/0\nA.md", "0\n/5\nA.md", "0\n/0\n/0\n/0\nA.md")

        assertEquals(
            expected = broken.map { null },
            actual = broken.map { text ->
                settings.putString(key = key, value = text)
                store.read(vaultPath = "/v")
            }
        )
    }
}

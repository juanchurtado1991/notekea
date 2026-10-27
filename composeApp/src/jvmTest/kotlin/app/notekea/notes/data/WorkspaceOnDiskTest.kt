package app.notekea.notes.data

import app.notekea.core.data.PropertiesFileSettingsFactory
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.Workspace
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A vault's tabs on the real settings file (`ROADMAP.md` 1.11): written by one run and read back by the next, paths
 * with spaces, accents and folders included.
 */
@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class WorkspaceOnDiskTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    private val appData = createTempDirectory(prefix = TEMP_PREFIX)

    @AfterTest
    fun tearDown() {
        appData.deleteRecursively()
    }

    @Test
    fun `the tabs and the split of one run are back in the next`() = runTest(context = testDispatcher) {
        val workspace = listOf(
            "Recipes/Café con leche.md",
            "Journal/2026-10-21.md",
            "attachments",
            "Notes with spaces.md"
        )
            .map { RelativePath(value = it) }
            .fold(initial = Workspace.EMPTY) { it, path -> it.open(path = path) }
            .splitRight()
        store().write(vaultPath = VAULT, workspace = workspace)

        assertEquals(expected = workspace, actual = store().read(vaultPath = VAULT))
    }

    /** A store over the settings file as a new run of the app opens it. */
    private fun store(): SettingsWorkspaceStore = SettingsWorkspaceStore(
        settings = PropertiesFileSettingsFactory(appDataDirectory = appData.toString()).create(),
        dispatchers = dispatchers
    )

    companion object {
        private const val TEMP_PREFIX = "notekea-workspace"
        private const val VAULT = "/home/ana/Personal Notes"
    }
}

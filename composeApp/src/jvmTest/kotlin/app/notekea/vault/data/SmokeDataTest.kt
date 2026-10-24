package app.notekea.vault.data

import app.notekea.core.domain.FakeAppDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

/** Keeps `smoke-data/README.md` true: the sample vaults hold exactly the note counts it documents. */
class SmokeDataTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `sample vaults hold the documented note counts`() = runTest(context = testDispatcher) {
        val vaultFileSystem = OkioVaultFileSystem(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = FakeAppDispatchers(dispatcher = testDispatcher),
            homeDirectory = VAULTS_DIRECTORY
        )

        assertEquals(
            expected = mapOf(PERSONAL to 12, WORK to 6, EMPTY to 0),
            actual = listOf(PERSONAL, WORK, EMPTY).associateWith {
                vaultFileSystem.countNotes(path = (VAULTS_DIRECTORY.toPath() / it).toString())
            }
        )
    }

    companion object {
        private const val EMPTY = "Empty Vault"
        private const val PERSONAL = "Personal Notes"
        private const val VAULTS_DIRECTORY = "../smoke-data/vaults"
        private const val WORK = "Work Obsidian"
    }
}

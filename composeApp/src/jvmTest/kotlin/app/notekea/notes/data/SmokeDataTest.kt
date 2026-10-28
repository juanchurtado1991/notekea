package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Keeps `smoke-data/README.md` true: the sample vaults hold exactly the note counts it documents, as the vault tree
 * counts them (hidden folders and files that are not Markdown left out).
 */
class SmokeDataTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `sample vaults hold the documented note counts`() = runTest(context = testDispatcher) {
        val scanner = OkioNoteScanner(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
        )

        assertEquals(
            expected = mapOf(PERSONAL to 12, WORK to 6, EMPTY to 0, SAMPLE to 40, GHOST to 371),
            actual = listOf(PERSONAL, WORK, EMPTY, SAMPLE, GHOST).associateWith {
                val listing = scanner.scan(vaultPath = (VAULTS_DIRECTORY.toPath() / it).toString())
                (listing as Outcome.Success).value.files.size
            }
        )
    }

    companion object {
        private const val EMPTY = "Empty Vault"
        private const val GHOST = "Ghost Serializer"
        private const val PERSONAL = "Personal Notes"
        private const val SAMPLE = "Sample Vault"
        private const val VAULTS_DIRECTORY = "../smoke-data/vaults"
        private const val WORK = "Work Obsidian"
    }
}

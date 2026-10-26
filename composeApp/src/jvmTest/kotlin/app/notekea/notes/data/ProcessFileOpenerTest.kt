package app.notekea.notes.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class ProcessFileOpenerTest {
    private val commands = mutableListOf<List<String>>()
    private val vault = Files.createTempDirectory(TEMP_PREFIX).also {
        it.resolve(MENU.value).createParentDirectories().writeText(text = CONTENT)
    }

    @AfterTest
    fun deleteVault() {
        vault.deleteRecursively()
    }

    @Test
    fun `each system opens the file with its own command, and a failed one is reported`() = runBlocking {
        val file = vault.resolve(MENU.value).toString()
        val outcomes = listOf(
            OperatingSystem.LINUX to 0,
            OperatingSystem.MACOS to 0,
            OperatingSystem.WINDOWS to 0,
            OperatingSystem.LINUX to 4
        ).map { (system, exitCode) ->
            opener(operatingSystem = system, exitCode = exitCode).open(vault.toString(), MENU)
        }

        assertEquals(
            expected = listOf(
                listOf("xdg-open", file),
                listOf("open", file),
                listOf("cmd", "/c", "start", "\"\"", file),
                listOf("xdg-open", file)
            ),
            actual = commands
        )
        assertEquals(
            expected =
            List(size = 3) { Outcome.Success(value = Unit) } +
                Outcome.Failure(error = NotesError.OpenFailed(path = MENU.value)),
            actual = outcomes
        )
    }

    @Test
    fun `a file that is gone is reported without running anything`() = runBlocking {
        val outcome = opener(
            operatingSystem = OperatingSystem.LINUX,
            exitCode = 0
        ).open(vault.toString(), RelativePath(value = "gone.pdf"))

        assertEquals(expected = Outcome.Failure(error = NotesError.EntryMissing(path = "gone.pdf")), actual = outcome)
        assertEquals(expected = emptyList(), actual = commands)
    }

    private fun opener(
        operatingSystem: OperatingSystem,
        exitCode: Int
    ) = ProcessFileOpener(
        operatingSystem = operatingSystem,
        runCommand = {
            commands += it
            exitCode
        },
        dispatchers = JvmAppDispatchers()
    )

    companion object {
        private const val CONTENT = "%PDF"
        private const val TEMP_PREFIX = "notekea-opener"
        private val MENU = RelativePath(value = "attachments/menu.pdf")
    }
}

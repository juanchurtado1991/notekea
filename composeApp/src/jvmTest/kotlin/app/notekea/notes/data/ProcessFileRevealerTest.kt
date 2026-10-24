package app.notekea.notes.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class ProcessFileRevealerTest {
    private val commands = mutableListOf<List<String>>()
    private val vault: Path = Files.createTempDirectory(TEMP_PREFIX).also { root ->
        listOf(NOTE.value, LOOSE_NOTE).forEach { note ->
            root.resolve(note).createParentDirectories().writeText(text = CONTENT)
        }
    }

    @AfterTest
    fun deleteVault() {
        vault.deleteRecursively()
    }

    @Test
    fun `an entry that is gone is reported without opening anything, and so is a vault that moved`() = runBlocking {
        val linux = revealer(operatingSystem = OperatingSystem.LINUX, exitCodes = emptyList())
        val missingNote = linux.reveal(vaultPath = vault.toString(), path = RelativePath(value = "Gone.md"))
        val movedVault = linux.reveal(vaultPath = vault.resolve("Moved").toString(), path = NOTE)

        assertEquals(
            expected = listOf(
                Outcome.Failure(error = NotesError.EntryMissing(path = "Gone.md")),
                Outcome.Failure(error = NotesError.VaultMissing(path = vault.resolve("Moved").toString()))
            ),
            actual = listOf(missingNote, movedVault)
        )
        assertEquals(expected = emptyList(), actual = commands)
    }

    @Test
    fun `macOS selects the entry in Finder`() = runBlocking {
        val outcome = revealer(
            operatingSystem = OperatingSystem.MACOS,
            exitCodes = listOf(0)
        ).reveal(vaultPath = vault.toString(), path = NOTE)

        assertEquals(
            expected = Outcome.Success(value = Unit),
            actual = outcome
        )
        assertEquals(
            expected = listOf(listOf("open", "-R", entry())),
            actual = commands
        )
    }

    @Test
    fun `windows counts a started File Explorer as success whatever its exit code`() = runBlocking {
        val outcomes = listOf(1, null).map { exitCode ->
            revealer(
                operatingSystem = OperatingSystem.WINDOWS,
                exitCodes = listOf(exitCode)
            ).reveal(vaultPath = vault.toString(), path = NOTE)
        }

        assertEquals(
            expected = listOf(
                Outcome.Success(value = Unit),
                Outcome.Failure(error = NotesError.RevealFailed(path = NOTE.value))
            ),
            actual = outcomes
        )
        assertEquals(
            expected = listOf("explorer.exe", "/select,${entry()}"),
            actual = commands.first()
        )
    }

    @Test
    fun `linux asks the file manager over D-Bus with an escaped uri`() = runBlocking {
        val note = RelativePath(value = LOOSE_NOTE)

        revealer(
            operatingSystem = OperatingSystem.LINUX,
            exitCodes = listOf(0)
        ).reveal(vaultPath = vault.toString(), path = note)

        assertEquals(
            expected = "array:string:" + vault
                .resolve(note.value)
                .toUri()
                .toString()
                .replace(",", "%2C"),
            actual = commands.single()[6]
        )
        assertEquals(
            expected = listOf("dbus-send", "--session", "--dest=org.freedesktop.FileManager1"),
            actual = commands.single().take(n = 3)
        )
    }

    @Test
    fun `linux opens the folder when the file manager cannot select, and fails when nothing works`() = runBlocking {
        val opened = revealer(
            operatingSystem = OperatingSystem.LINUX,
            exitCodes = listOf(1, 0)
        ).reveal(vaultPath = vault.toString(), path = NOTE)
        val failed = revealer(
            operatingSystem = OperatingSystem.LINUX,
            exitCodes = listOf(null, 4)
        ).reveal(vaultPath = vault.toString(), path = NOTE)

        assertEquals(
            expected = listOf(
                Outcome.Success(value = Unit),
                Outcome.Failure(error = NotesError.RevealFailed(path = NOTE.value))
            ),
            actual = listOf(opened, failed)
        )
        assertEquals(
            expected = listOf("xdg-open", vault.resolve("Recipes").toString()),
            actual = commands[1]
        )
    }

    private fun entry(): String = vault.resolve(NOTE.value).toString()

    private fun revealer(
        operatingSystem: OperatingSystem,
        exitCodes: List<Int?>
    ): ProcessFileRevealer {
        val remaining = exitCodes.toMutableList()
        return ProcessFileRevealer(
            operatingSystem = operatingSystem,
            runCommand = { command ->
                commands += command
                remaining.removeFirst()
            },
            dispatchers = JvmAppDispatchers()
        )
    }

    companion object {
        private const val LOOSE_NOTE = "Ideas/Loose, unsorted.md"
        private const val TEMP_PREFIX = "notekea-reveal"
        private const val CONTENT = "# Note\n"
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}

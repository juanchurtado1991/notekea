package app.notekea.notes.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.runBlocking
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class ProcessFileRevealerTest {
    private val commands = mutableListOf<List<String>>()

    @Test
    fun `macOS selects the entry in Finder`() = runBlocking {
        val outcome = revealer(
            operatingSystem = OperatingSystem.MACOS,
            exitCodes = listOf(0)
        ).reveal(vaultPath = VAULT, path = NOTE)

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
            ).reveal(vaultPath = VAULT, path = NOTE)
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
        val note = RelativePath(value = "Ideas/Loose, unsorted.md")

        revealer(operatingSystem = OperatingSystem.LINUX, exitCodes = listOf(0)).reveal(vaultPath = VAULT, path = note)

        assertEquals(
            expected = "array:string:" + Path(VAULT, note.value).toUri().toString().replace(",", "%2C"),
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
        ).reveal(vaultPath = VAULT, path = NOTE)
        val failed = revealer(
            operatingSystem = OperatingSystem.LINUX,
            exitCodes = listOf(null, 4)
        ).reveal(vaultPath = VAULT, path = NOTE)

        assertEquals(
            expected = listOf(
                Outcome.Success(value = Unit),
                Outcome.Failure(error = NotesError.RevealFailed(path = NOTE.value))
            ),
            actual = listOf(opened, failed)
        )
        assertEquals(
            expected = listOf("xdg-open", Path(VAULT, "Recipes").toString()),
            actual = commands[1]
        )
    }

    private fun entry(): String = Path(VAULT, NOTE.value).toString()

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
        private const val VAULT = "/home/ana/Notes"
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}

package app.notekea.notes.data

import app.notekea.core.platform.domain.OperatingSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class JvmSystemTrashTest {
    @Test
    fun `linux trashes with gio and only a zero exit code counts`() {
        val commands = mutableListOf<List<String>>()
        val exitCodes = mutableListOf(0, 1, null)
        val trash = JvmSystemTrash(
            operatingSystem = OperatingSystem.LINUX,
            runCommand = { command ->
                commands += command
                exitCodes.removeFirst()
            }
        )

        assertEquals(
            expected = listOf(true, false, false),
            actual = List(size = 3) { trash.moveToTrash(absolutePath = NOTE) }
        )
        assertEquals(
            expected = listOf("gio", "trash", "--", NOTE),
            actual = commands.first()
        )
    }

    companion object {
        private const val NOTE = "/home/ana/Notes/-dash.md"
    }
}

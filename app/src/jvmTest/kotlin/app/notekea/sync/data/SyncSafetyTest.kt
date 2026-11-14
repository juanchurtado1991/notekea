package app.notekea.sync.data

import app.notekea.sync.domain.SyncError
import java.nio.file.Files
import kotlin.io.path.createFile
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The code review of 2026-11-13: a second conflict of a note overwrote the copy the first one kept, "keep both"
 * failed when "Note (mine).md" was there, and an address written with its token showed it in the error.
 */
class SyncSafetyTest {
    @Test
    fun `a name already taken gives the next free one beside it, a free one itself`() {
        val folder = Files.createTempDirectory("notekea-free")
        val mine = folder.resolve("Plan (mine).md")
        val free = FreeFile.beside(file = mine.toFile()).name
        mine.createFile()
        val second = FreeFile.beside(file = mine.toFile()).name
        folder.resolve(second).createFile()

        assertEquals(
            expected = listOf("Plan (mine).md", "Plan (mine) 2.md", "Plan (mine) 3.md"),
            actual = listOf(free, second, FreeFile.beside(file = mine.toFile()).name)
        )
    }

    @Test
    fun `an error never shows the token an address was written with`() {
        val error = JGitErrors.of(
            throwable = IllegalStateException("https://ghp_secret123@github.com/me/notes.git: not found")
        )

        assertEquals(
            expected = SyncError.Failed(message = "https://github.com/me/notes.git: not found"),
            actual = error
        )
    }
}

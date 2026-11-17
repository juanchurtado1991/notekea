package app.notekea.sync.data

import app.notekea.sync.domain.SyncError
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.TransportException
import java.io.File
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

    // Bug hunt, 2026-11-17: a conflict copy whose move failed stayed where it was, and the next was written over it.
    @Test
    fun `a move aside that cannot happen throws and leaves the file where it was`() {
        val folder = Files.createTempDirectory("notekea-move").toFile()
        val copy = File(folder, "Plan (conflict).md").apply { writeText("The first conflict.") }
        val unreachable = File(folder, "Gone/Plan (mine).md")

        val isThrown = runCatching {
            FreeFile.moveBeside(
                file = copy,
                wanted = unreachable
            )
        }.isFailure

        assertEquals(
            expected = true to "The first conflict.",
            actual = isThrown to copy.readText()
        )
    }

    // Bug hunt, 2026-11-17: a "401" in the address made any error a refused token, and the token was deleted.
    @Test
    fun `a transport error is a refused token by what Git says, never by the numbers in the address`() {
        val errors = listOf(
            "https://example.com/team401/notes403.git: connection reset",
            "https://example.com/me/notes.git: not authorized",
            "https://example.com/me/notes.git: git-receive-pack not permitted on 'https://example.com/me/notes.git/'"
        ).map { JGitErrors.of(throwable = TransportException(it)) }

        assertEquals(
            expected = listOf(
                SyncError.Failed(message = "https://example.com/team401/notes403.git: connection reset"),
                SyncError.AuthFailed,
                SyncError.AuthFailed
            ),
            actual = errors
        )
    }

    // A file over the host's limit, once committed, had every push refused for good.
    @Test
    fun `a new file over the limit is left out of the repository, and the rest is still seen`() {
        val folder = Files.createTempDirectory("notekea-large").toFile()

        val untracked = Git
            .init()
            .setDirectory(folder)
            .call()
            .use { git ->
                File(folder, "Big [1].mp4").writeBytes(ByteArray(size = BIG))
                File(folder, "Note.md").writeText("A note.")

                val left = LargeFiles.leaveOut(
                    git = git,
                    untracked = git
                        .status()
                        .call()
                        .untracked,
                    maxBytes = LIMIT
                )

                left to git
                    .status()
                    .call()
                    .untracked
            }

        assertEquals(
            expected = listOf("Big [1].mp4") to setOf("Note.md"),
            actual = untracked
        )
    }

    private companion object {
        const val BIG = 2_000
        const val LIMIT = 1_000L
    }
}

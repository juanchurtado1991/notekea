package app.notekea.sync.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.SyncError
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.treewalk.TreeWalk
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * The bug hunt of 2026-11-17, on real repositories: a repository inside the vault was committed as a gitlink, a
 * change that left nothing to commit made an empty commit, and a lock left by a killed sync blocked it for good.
 */
class JGitCommitTest {
    private val vault = Files.createTempDirectory("notekea-commit").toFile()
    private val git = JGitRepository(
        dispatchers = JvmAppDispatchers(),
        clock = Clock.System
    )

    @AfterTest
    fun tearDown() {
        vault.deleteRecursively()
    }

    private fun write(
        path: String,
        text: String
    ) = File(vault, path).apply { parentFile.mkdirs() }.writeText(text)

    private fun head() = Git.open(vault).use { it.repository.resolve(Constants.HEAD) }

    private fun committed(path: String): Boolean = Git.open(vault).use {
        val tree = it
            .repository
            .parseCommit(it.repository.resolve(Constants.HEAD))
            .tree

        TreeWalk
            .forPath(it.repository, path, tree)
            ?.use { true } ?: false
    }

    private suspend fun commit(): Outcome<Boolean, SyncError> = git.commitAll(
        vaultPath = vault.path,
        message = "work"
    )

    private suspend fun started() {
        write(
            path = "Plan.md",
            text = "# Plan\n"
        )

        Git
            .init()
            .setDirectory(vault)
            .call()
            .close()

        commit()
    }

    @Test
    fun `a folder holding a repository of its own is left out, and the rest is committed`() =
        runBlocking {
            Git
                .init()
                .setDirectory(File(vault, "Code"))
                .call()
                .use { nested ->
                    write(
                        path = "Code/README.md",
                        text = "# Code\n"
                    )

                    nested
                        .add()
                        .addFilepattern(".")
                        .call()

                    nested
                        .commit()
                        .setMessage("code")
                        .call()
                }

            started()

            assertEquals(
                expected = listOf(true, false, true),
                actual = listOf(
                    committed(path = "Plan.md"),
                    committed(path = "Code"),
                    "/Code" in File(vault, ".git/info/exclude").readLines()
                )
            )
        }

    @Test
    fun `a change that leaves nothing to commit makes no commit`() = runBlocking {
        started()
        val before = head()

        // A new file over the host's limit is left out: the status was not clean, and nothing is left to commit.
        RandomAccessFile(File(vault, "Film.mp4"), "rw").use {
            it.setLength(LargeFiles.MAX_BYTES + 1)
        }

        assertEquals(
            expected = Outcome.Success(value = false) to before,
            actual = commit() to head()
        )
    }

    // The code review of 2026-11-22: only new files were measured, so a synced one that grew
    // past the limit was committed.
    @Test
    fun `a file already committed that grows past the limit leaves the repository and stays in the vault`() =
        runBlocking {
            started()
            val film = File(vault, "Film.mp4")

            write(
                path = "Film.mp4",
                text = "A short cut."
            )

            commit()
            val wasCommitted = committed(path = "Film.mp4")

            RandomAccessFile(film, "rw").use { it.setLength(LargeFiles.MAX_BYTES + 1) }

            commit()

            assertEquals(
                expected = listOf(true, false, true, Outcome.Success(value = false)),
                actual = listOf(
                    wasCommitted,
                    committed(path = "Film.mp4"),
                    film.length() > LargeFiles.MAX_BYTES,
                    commit()
                )
            )
        }

    // The dialog that tells the person (the code review of 2026-11-22) names what a commit left out. They wait with
    // the repository, so the app closing, or the commit failing afterwards, does not lose them (2026-11-23).
    @Test
    fun `the files left out for their size wait with the repository until the person read them`() =
        runBlocking {
            started()

            RandomAccessFile(File(vault, "Film.mp4"), "rw").use {
                it.setLength(LargeFiles.MAX_BYTES + 1)
            }

            commit()

            val reopened = JGitRepository(
                dispatchers = JvmAppDispatchers(),
                clock = Clock.System
            )

            val waiting = reopened.leftOut(vaultPath = vault.path)

            reopened.acknowledgeLeftOut(
                vaultPath = vault.path,
                files = waiting
            )

            assertEquals(
                expected = listOf(listOf(RelativePath(value = "Film.mp4")), emptyList()),
                actual = listOf(waiting, git.leftOut(vaultPath = vault.path))
            )
        }

    @Test
    fun `a lock left by a sync killed minutes ago is removed, a fresh one is not`() = runBlocking {
        started()
        val lock = File(vault, ".git/index.lock")

        lock.writeText("")

        write(
            path = "Plan.md",
            text = "# Plan\n\nWhile a sync holds the lock.\n"
        )

        val whileHeld = commit() is Outcome.Failure
        lock.setLastModified(System.currentTimeMillis() - 10.minutes.inWholeMilliseconds)

        assertEquals(
            expected = listOf(true, Outcome.Success(value = true), false),
            actual = listOf(whileHeld, commit(), lock.exists())
        )
    }
}

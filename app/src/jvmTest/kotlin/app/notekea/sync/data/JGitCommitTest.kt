package app.notekea.sync.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
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

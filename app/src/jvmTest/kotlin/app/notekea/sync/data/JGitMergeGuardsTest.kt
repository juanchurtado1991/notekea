package app.notekea.sync.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.sync.domain.SyncError
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryState
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * The bug hunt of 2026-11-17, on real repositories: a merge that did not happen was a pull that worked, and a note
 * against a folder of its name was committed with one side only.
 */
class JGitMergeGuardsTest {
    private val root = Files.createTempDirectory("notekea-merge-guards").toFile()
    private val remote = File(root, "remote.git")
    private val laptop = File(root, "Laptop")
    private val desktop = File(root, "Desktop")
    private val git = JGitRepository(
        dispatchers = JvmAppDispatchers(),
        clock = Clock.System
    )
    private val url = remote.toURI().toString()

    init {
        Git
            .init()
            .setBare(true)
            .setDirectory(remote)
            .setInitialBranch("main")
            .call()
            .close()

        laptop.mkdirs()
        desktop.mkdirs()
    }

    @AfterTest
    fun tearDown() {
        root.deleteRecursively()
    }

    private fun File.write(
        path: String,
        text: String
    ) = File(this, path).apply { parentFile.mkdirs() }.writeText(text)

    private fun File.read(path: String): String? = File(this, path).takeIf { it.isFile }?.readText()

    private fun File.state(): RepositoryState = Git.open(this).use { it.repository.repositoryState }

    private suspend fun bothConnected() {
        laptop.write(
            path = "Plan.md",
            text = "# Plan\n"
        )

        listOf(laptop, desktop).forEach {
            git.connect(
                vaultPath = it.path,
                remoteUrl = url,
                token = null
            )
        }
    }

    private suspend fun commitAndPush(
        vault: File,
        path: String,
        text: String
    ) {
        vault.write(
            path = path,
            text = text
        )

        git.commitAll(
            vaultPath = vault.path,
            message = "work"
        )

        git.push(
            vaultPath = vault.path,
            token = null
        )
    }

    private fun messageOf(outcome: Outcome<*, SyncError>): String? =
        ((outcome as? Outcome.Failure)?.error as? SyncError.Failed)?.message

    @Test
    fun `a merge stopped by a note written meanwhile is a failed pull that names it`() =
        runBlocking {
            bothConnected()

            commitAndPush(
                vault = laptop,
                path = "Plan.md",
                text = "# Plan\n\nThe laptop's draft.\n"
            )

            desktop.write(
                path = "Ideas.md",
                text = "# Ideas\n"
            )

            git.commitAll(
                vaultPath = desktop.path,
                message = "work"
            )

            // Written after the commit and before the pull, as a save that lands between the two.
            desktop.write(
                path = "Plan.md",
                text = WRITTEN_MEANWHILE
            )

            val pulled = git.pull(
                vaultPath = desktop.path,
                token = null
            )

            assertEquals(
                expected = listOf<Any?>(true, WRITTEN_MEANWHILE, RepositoryState.SAFE),
                actual = listOf(
                    messageOf(outcome = pulled)?.contains(other = "Plan.md"),
                    desktop.read(path = "Plan.md"),
                    desktop.state()
                )
            )
        }

    @Test
    fun `a note here against a folder of its name on the remote fails the pull, the note kept`() =
        runBlocking {
            bothConnected()

            commitAndPush(
                vault = laptop,
                path = "Projects/Idea.md",
                text = IDEA
            )

            desktop.write(
                path = "Projects",
                text = NOTE
            )

            git.commitAll(
                vaultPath = desktop.path,
                message = "work"
            )

            val pulled = git.pull(
                vaultPath = desktop.path,
                token = null
            )

            assertEquals(
                expected = listOf<Any?>(true, NOTE, RepositoryState.SAFE),
                actual = listOf(
                    messageOf(outcome = pulled)?.contains(other = "Projects"),
                    desktop.read(path = "Projects"),
                    desktop.state()
                )
            )
        }

    private companion object {
        const val WRITTEN_MEANWHILE = "# Plan\n\nWritten between the commit and the pull.\n"
        const val IDEA = "# Idea\n"
        const val NOTE = "A note named like the remote's folder.\n"
    }
}

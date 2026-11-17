package app.notekea.sync.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.RepairChoice
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
 * The bug hunt of 2026-11-17, on real repositories: a rebase cut short listed the whole vault as changed here and
 * "take remote" lost the commits not replayed yet; connecting or settling a conflict mid-merge committed its marks.
 */
class JGitRebaseRepairTest {
    private val root = Files.createTempDirectory("notekea-rebase-repair").toFile()
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

    private fun trashed(): Map<String, String> {
        val trash = File(desktop, ".trash/Sync repair")

        return trash
            .walkTopDown()
            .filter { it.isFile }
            .associate {
                it.relativeTo(trash).invariantSeparatorsPath to it.readText()
            }
    }

    /**
     * Both computers share Plan, Ideas and Shared; each changes Plan its own way and the laptop's reaches the remote.
     * The desktop also wrote Later in a commit of its own, then was left in the middle of rebasing both on the
     * laptop's side, stopped at Plan, with Later not replayed yet. Then the person writes on in Ideas.
     */
    private fun leftHalfWayInARebase() = runBlocking {
        listOf("Plan.md", "Ideas.md", "Shared.md").forEach {
            laptop.write(
                path = it,
                text = "# $it\n"
            )
        }

        connect(vault = laptop)
        connect(vault = desktop)

        laptop.write(
            path = "Plan.md",
            text = LAPTOP_PLAN
        )

        commit(vault = laptop)

        git.push(
            vaultPath = laptop.path,
            token = null
        )

        desktop.write(
            path = "Plan.md",
            text = DESKTOP_PLAN
        )

        commit(vault = desktop)

        desktop.write(
            path = "Later.md",
            text = LATER
        )

        commit(vault = desktop)

        Git.open(desktop).use { opened ->
            opened.fetch().call()

            opened
                .rebase()
                .setUpstream(opened.repository.resolve("refs/remotes/origin/main"))
                .call()
        }

        desktop.write(
            path = "Ideas.md",
            text = AFTER_CRASH
        )
    }

    private suspend fun connect(vault: File) {
        git.connect(
            vaultPath = vault.path,
            remoteUrl = url,
            token = null
        )
    }

    private suspend fun commit(vault: File) {
        git.commitAll(
            vaultPath = vault.path,
            message = "work"
        )
    }

    @Test
    fun `a rebase cut short lists the notes changed here, not the whole vault`() = runBlocking {
        leftHalfWayInARebase()

        assertEquals(
            expected = true to listOf("Ideas.md", "Later.md", "Plan.md"),
            actual = desktop.state().isRebasing to git
                .unfinished(vaultPath = desktop.path)
                ?.changedHere
                ?.map { it.value }
        )
    }

    @Test
    fun `going back to the remote from a rebase cut short keeps the commits not replayed in the trash`() =
        runBlocking {
            leftHalfWayInARebase()

            git.repair(
                vaultPath = desktop.path,
                choice = RepairChoice.TAKE_REMOTE
            )

            val trash = trashed()

            assertEquals(
                expected = listOf<Any?>(
                    RepositoryState.SAFE,
                    LAPTOP_PLAN,
                    null,
                    LATER,
                    AFTER_CRASH,
                    true,
                    false
                ),
                actual = listOf(
                    desktop.state(),
                    desktop.read(path = "Plan.md"),
                    desktop.read(path = "Later.md"),
                    trash["Later.md"],
                    trash["Ideas.md"],
                    DESKTOP_PLAN in trash.values,
                    "Shared.md" in trash
                )
            )
        }

    @Test
    fun `connecting or settling a conflict in the middle of a rebase commits nothing`() =
        runBlocking {
            leftHalfWayInARebase()

            desktop.write(
                path = "Plan (conflict).md",
                text = DESKTOP_PLAN
            )

            val head = Git.open(desktop).use { it.repository.resolve("HEAD") }

            val connected = git.connect(
                vaultPath = desktop.path,
                remoteUrl = url,
                token = null
            )

            val resolved = git.resolve(
                vaultPath = desktop.path,
                note = RelativePath(value = "Plan.md"),
                choice = ConflictChoice.THEIRS
            )

            assertEquals(
                expected = listOf<Any?>(true, true, head, true),
                actual = listOf(
                    connected is Outcome.Failure,
                    resolved is Outcome.Failure,
                    Git.open(desktop).use { it.repository.resolve("HEAD") },
                    desktop.state().isRebasing
                )
            )
        }

    private companion object {
        const val LAPTOP_PLAN = "# Plan\n\nThe laptop's draft.\n"
        const val DESKTOP_PLAN = "# Plan\n\nThe desktop's draft.\n"
        const val LATER = "# Later\n\nWritten here after the draft, never pushed.\n"
        const val AFTER_CRASH = "# Ideas\n\nWritten after the crash.\n"
    }
}

package app.notekea.sync.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.sync.domain.RepairChoice
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryState
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A vault left in the middle of a merge or a rebase (the app killed half way), on real repositories: nothing is
 * repaired by itself, and either repair keeps what the person wrote (left open on 2026-11-13, done on 2026-11-14).
 */
class JGitRepairTest {
    private val root = Files.createTempDirectory("notekea-repair").toFile()
    private val remote = File(root, "remote.git")
    private val laptop = File(root, "Laptop")
    private val desktop = File(root, "Desktop")
    private val git = JGitRepository(dispatchers = JvmAppDispatchers())
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

    /**
     * Both computers have Plan and Ideas; each changes Plan its own way, the laptop's reaches the remote, and the
     * desktop is left as a crash leaves it: in the middle of bringing the laptop's side, Plan full of Git's marks.
     * Then the person writes on in Ideas, not knowing.
     */
    private fun leftHalfWay(isRebase: Boolean) = runBlocking {
        laptop.write(
            path = "Plan.md",
            text = "# Plan\n\nThe first draft.\n"
        )

        laptop.write(
            path = "Ideas.md",
            text = "# Ideas\n"
        )

        git.connect(
            vaultPath = laptop.path,
            remoteUrl = url,
            token = null
        )

        git.connect(
            vaultPath = desktop.path,
            remoteUrl = url,
            token = null
        )

        laptop.write(
            path = "Plan.md",
            text = "# Plan\n\nThe laptop's draft.\n"
        )

        git.commitAll(
            vaultPath = laptop.path,
            message = "laptop"
        )

        git.push(
            vaultPath = laptop.path,
            token = null
        )

        desktop.write(
            path = "Plan.md",
            text = "# Plan\n\nThe desktop's draft.\n"
        )

        git.commitAll(
            vaultPath = desktop.path,
            message = "desktop"
        )

        Git.open(desktop).use { opened ->
            opened.fetch().call()
            val theirs = opened.repository.resolve("refs/remotes/origin/main")

            if (isRebase) {
                opened
                    .rebase()
                    .setUpstream(theirs)
                    .call()
            } else {
                opened
                    .merge()
                    .include(theirs)
                    .call()
            }
        }

        desktop.write(
            path = "Ideas.md",
            text = "# Ideas\n\nWritten after the crash.\n"
        )
    }

    @Test
    fun `a vault left in the middle of a merge is told, with the notes changed here, and nothing else is`() =
        runBlocking {
            leftHalfWay(isRebase = false)

            assertEquals(
                expected = listOf<Any?>(
                    RepositoryState.MERGING,
                    listOf("Ideas.md", "Plan.md"),
                    null,
                    true
                ),
                actual = listOf<Any?>(
                    desktop.state(),
                    git
                        .unfinished(vaultPath = desktop.path)
                        ?.changedHere
                        ?.map { it.value },
                    git.unfinished(vaultPath = laptop.path),
                    "<<<<<<<" in desktop.read(path = "Plan.md").orEmpty()
                )
            )
        }

    @Test
    fun `keeping mine undoes the merge, keeps what was written since, and the next sync keeps both sides`() =
        runBlocking {
            leftHalfWay(isRebase = false)

            git.repair(
                vaultPath = desktop.path,
                choice = RepairChoice.KEEP_MINE
            )

            val repaired =
                listOf(desktop.state(), desktop.read(path = "Plan.md"), desktop.read(path = "Ideas.md"))

            git.commitAll(
                vaultPath = desktop.path,
                message = "after the repair"
            )

            git.pull(
                vaultPath = desktop.path,
                token = null
            )

            assertEquals(
                expected = listOf<Any?>(
                    RepositoryState.SAFE,
                    "# Plan\n\nThe desktop's draft.\n",
                    "# Ideas\n\nWritten after the crash.\n",
                    "# Plan\n\nThe laptop's draft.\n",
                    "# Plan\n\nThe desktop's draft.\n",
                    null
                ),
                actual = repaired + listOf(
                    desktop.read(path = "Plan.md"),
                    desktop.read(path = "Plan (conflict).md"),
                    git.unfinished(vaultPath = desktop.path)
                )
            )
        }

    // Code review, 2026-11-15: a clashing note the person wrote in after the crash went back to the last commit.
    @Test
    fun `keeping mine copies a clashing note, as the person left it, to the vault's trash`() =
        runBlocking {
            leftHalfWay(isRebase = false)

            desktop.write(
                path = "Plan.md",
                text = HAND_MERGED
            )

            git.repair(
                vaultPath = desktop.path,
                choice = RepairChoice.KEEP_MINE
            )

            assertEquals(
                expected = "# Plan\n\nThe desktop's draft.\n" to HAND_MERGED,
                actual = desktop.read(path = "Plan.md") to desktop.read(path = ".trash/Sync repair/Plan.md")
            )
        }

    @Test
    fun `a rebase cut short is repaired the same way`() = runBlocking {
        leftHalfWay(isRebase = true)
        val before = desktop.state().isRebasing

        git.repair(
            vaultPath = desktop.path,
            choice = RepairChoice.KEEP_MINE
        )

        assertEquals(
            expected = listOf<Any?>(
                true,
                RepositoryState.SAFE,
                "# Plan\n\nThe desktop's draft.\n",
                "# Ideas\n\nWritten after the crash.\n"
            ),
            actual = listOf<Any?>(
                before,
                desktop.state(),
                desktop.read(path = "Plan.md"),
                desktop.read(path = "Ideas.md")
            )
        )
    }

    @Test
    fun `going back to the remote takes its notes, and what was changed here is in the vault's trash`() =
        runBlocking {
            leftHalfWay(isRebase = false)

            git.repair(
                vaultPath = desktop.path,
                choice = RepairChoice.TAKE_REMOTE
            )

            val kept = File(desktop, ".trash")
                .walkTopDown()
                .filter { it.isFile }
                .associate {
                    it.name to it.readText()
                }

            assertEquals(
                expected = listOf<Any?>(
                    RepositoryState.SAFE,
                    "# Plan\n\nThe laptop's draft.\n",
                    "# Ideas\n",
                    "# Ideas\n\nWritten after the crash.\n",
                    true,
                    0
                ),
                actual = listOf<Any?>(
                    desktop.state(),
                    desktop.read(path = "Plan.md"),
                    desktop.read(path = "Ideas.md"),
                    kept["Ideas.md"],
                    "Plan.md" in kept,
                    git.pendingChanges(vaultPath = desktop.path)
                )
            )
        }

    @Test
    fun `a vault that is in the middle of nothing is left as it is by a repair`() = runBlocking {
        laptop.write(
            path = "Plan.md",
            text = "# Plan\n"
        )

        git.connect(
            vaultPath = laptop.path,
            remoteUrl = url,
            token = null
        )

        laptop.write(
            path = "Plan.md",
            text = "# Plan\n\nNot committed yet.\n"
        )

        git.repair(
            vaultPath = laptop.path,
            choice = RepairChoice.TAKE_REMOTE
        )

        assertEquals(
            expected = "# Plan\n\nNot committed yet.\n" to false,
            actual = laptop.read(path = "Plan.md") to File(laptop, ".trash").exists()
        )
    }

    private companion object {
        const val HAND_MERGED = "# Plan\n\nThe laptop's draft and the desktop's, joined by hand.\n"
    }
}

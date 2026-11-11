package app.notekea.sync.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.PullResult
import app.notekea.sync.domain.SyncError
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.TransportException
import java.io.File
import java.net.UnknownHostException
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Sync on real repositories: two vaults of one remote, a bare repository on disk (`file://`, no network), through
 * [JGitRepository] as the app uses it.
 */
class JGitRepositoryTest {
    private val root = Files.createTempDirectory("notekea-sync").toFile()
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

    private fun sync(vault: File): Outcome<PullResult, SyncError> = runBlocking {
        git.commitAll(
            vaultPath = vault.path,
            message = "change"
        )

        val pulled = git.pull(
            vaultPath = vault.path,
            token = null
        )

        git.push(
            vaultPath = vault.path,
            token = null
        )

        pulled
    }

    @Test
    fun `a vault connected to an empty remote reaches a second vault, and an edit comes back on pull`() {
        laptop.write(
            path = "Ideas/Plan.md",
            text = "# Plan\n"
        )

        laptop.write(
            path = "Plan.md.notekea-tmp",
            text = "half a save"
        )

        runBlocking {
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
        }

        laptop.write(
            path = "Ideas/Plan.md",
            text = "# Plan\n\nBuy seeds.\n"
        )

        sync(vault = laptop)
        val pulled = sync(vault = desktop) as Outcome.Success

        assertEquals(
            expected = listOf<Any?>("# Plan\n\nBuy seeds.\n", null, 1, emptyList<RelativePath>()),
            actual = listOf(
                desktop.read(path = "Ideas/Plan.md"),
                desktop.read(path = "Plan.md.notekea-tmp"),
                pulled.value.changed,
                pulled.value.conflicts
            )
        )
    }

    @Test
    fun `a note changed on both sides keeps the remote's version and ours beside it, and each choice settles it`() {
        laptop.write(
            path = "Plan.md",
            text = "first\n"
        )

        runBlocking {
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
        }

        laptop.write(
            path = "Plan.md",
            text = "laptop\n"
        )

        laptop.write(
            path = "Other.md",
            text = "only on the laptop\n"
        )

        sync(vault = laptop)

        desktop.write(
            path = "Plan.md",
            text = "desktop\n"
        )

        val pulled = sync(vault = desktop) as Outcome.Success
        val conflicts = runBlocking { git.conflicts(vaultPath = desktop.path) }

        val texts = listOf(
            desktop.read(path = "Plan.md"),
            desktop.read(path = "Plan (conflict).md"),
            desktop.read(path = "Other.md")
        )

        runBlocking {
            git.resolve(desktop.path, RelativePath(value = "Plan.md"), ConflictChoice.BOTH)
        }

        assertEquals(
            expected = listOf<Any?>(
                listOf(RelativePath(value = "Plan.md")),
                listOf(RelativePath(value = "Plan.md")),
                listOf("laptop\n", "desktop\n", "only on the laptop\n"),
                "desktop\n",
                emptyList<RelativePath>()
            ),
            actual = listOf(
                pulled.value.conflicts,
                conflicts,
                texts,
                desktop.read(path = "Plan (mine).md"),
                runBlocking { git.conflicts(vaultPath = desktop.path) }
            )
        )
    }

    @Test
    fun `keep mine puts our version in the note, keep theirs drops it`() {
        desktop.write(
            path = "A.md",
            text = "theirs\n"
        )

        desktop.write(
            path = "A (conflict).md",
            text = "mine\n"
        )

        desktop.write(
            path = "B.md",
            text = "theirs\n"
        )

        desktop.write(
            path = "B (conflict).md",
            text = "mine\n"
        )

        runBlocking {
            git.connect(
                vaultPath = desktop.path,
                remoteUrl = url,
                token = null
            )
        }

        runBlocking {
            git.resolve(desktop.path, RelativePath(value = "A.md"), ConflictChoice.MINE)
            git.resolve(desktop.path, RelativePath(value = "B.md"), ConflictChoice.THEIRS)
        }

        assertEquals(
            expected = listOf<String?>("mine\n", null, "theirs\n", null),
            actual = listOf("A.md", "A (conflict).md", "B.md", "B (conflict).md").map {
                desktop.read(path = it)
            }
        )
    }

    @Test
    fun `changes not pushed are counted, and none once they are`() {
        laptop.write(
            path = "Plan.md",
            text = "first\n"
        )

        runBlocking {
            git.connect(
                vaultPath = laptop.path,
                remoteUrl = url,
                token = null
            )
        }

        laptop.write(
            path = "Plan.md",
            text = "second\n"
        )

        laptop.write(
            path = "New.md",
            text = "new\n"
        )

        val before = runBlocking { git.pendingChanges(vaultPath = laptop.path) }

        runBlocking {
            git.commitAll(
                vaultPath = laptop.path,
                message = "edit"
            )
        }

        val committed = runBlocking { git.pendingChanges(vaultPath = laptop.path) }
        sync(vault = laptop)

        assertEquals(
            expected = listOf(2, 2, 0),
            actual = listOf(
                before,
                committed,
                runBlocking { git.pendingChanges(vaultPath = laptop.path) }
            )
        )
    }

    @Test
    fun `a new vault joins a remote whose history is on master`() {
        val other = File(root, "Other")

        Git
            .cloneRepository()
            .setURI(url)
            .setDirectory(other)
            .call()
            .use { clone ->
                File(other, "Old.md").writeText("old\n")

                clone
                    .add()
                    .addFilepattern(".")
                    .call()

                clone
                    .commit()
                    .setMessage("old")
                    .setAuthor("A", "a@b")
                    .call()

                clone
                    .push()
                    .setRemote("origin")
                    .add("master")
                    .call()
            }

        laptop.write(
            path = "New.md",
            text = "new\n"
        )

        runBlocking {
            git.connect(
                vaultPath = laptop.path,
                remoteUrl = url,
                token = null
            )
        }

        val branch = Git.open(laptop).use { it.repository.branch }

        assertEquals(
            expected = listOf<Any?>("master", "old\n", "new\n"),
            actual = listOf(branch, laptop.read(path = "Old.md"), laptop.read(path = "New.md"))
        )
    }

    @Test
    fun `a refused token, no network and other failures are told apart`() {
        assertEquals(
            expected = listOf(SyncError.AuthFailed, SyncError.Offline, SyncError.Failed(message = "boom")),
            actual = listOf(
                JGitErrors.of(throwable = TransportException("https://x: not authorized")),
                JGitErrors.of(throwable = TransportException("https://x", UnknownHostException("x"))),
                JGitErrors.of(throwable = IllegalStateException("boom"))
            )
        )
    }
}

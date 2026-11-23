package app.notekea.sync.data

import app.notekea.core.data.JvmAppDispatchers
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryState
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.time.Clock

/**
 * Two computers, [laptop] and [desktop], and the bare [remote] they sync with at [url], in a temporary folder named
 * [name]: what the tests of sync over real repositories start from.
 */
abstract class TwoComputers(name: String) {
    protected val root: File = Files.createTempDirectory(name).toFile()
    protected val remote = File(root, "remote.git")
    protected val laptop = File(root, "Laptop")
    protected val desktop = File(root, "Desktop")
    protected val url: String = remote.toURI().toString()

    protected val git = JGitRepository(
        dispatchers = JvmAppDispatchers(),
        clock = Clock.System
    )

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

    protected fun File.write(
        path: String,
        text: String
    ) = File(this, path).apply { parentFile.mkdirs() }.writeText(text)

    protected fun File.read(path: String): String? =
        File(this, path).takeIf { it.isFile }?.readText()

    protected fun File.state(): RepositoryState =
        Git.open(this).use { it.repository.repositoryState }
}

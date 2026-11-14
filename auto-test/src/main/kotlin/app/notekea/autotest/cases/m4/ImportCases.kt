package app.notekea.autotest.cases.m4

import app.notekea.autotest.expectFolder
import app.notekea.autotest.notekea
import com.testbuddy.CaseScope
import com.testbuddy.SuiteBuilder
import org.eclipse.jgit.api.Git
import java.io.File
import kotlin.time.Duration.Companion.minutes

private const val REMOTE = "import-remote.git"
private const val SEED = "import-seed"
private const val NOTE_NAME = "From the repository"
private val IMPORT_TIMEOUT = 3.minutes

/**
 * Import a vault from a repository, on the welcome screen (the developer's request of 2026-11-14): a repository on
 * disk made by the case (`file://`), so no account or network is needed. What is no address is said under the
 * address; the real one brings the notes into a new folder, which opens as the vault, already syncing.
 */
internal fun SuiteBuilder.importCases() {
    automated(
        id = "1.3-11",
        title = "Import from a repository says a bad address, then brings the notes into a new folder and opens it",
        timeout = IMPORT_TIMEOUT
    ) {
        val remote = seededRemote()

        // The folder goes into the case's data, never into the person's Documents.
        notekea().folderPicker.nextPick = file(path = "").toString()

        click(text = "Import from a repository…")
        expectText(text = "Import a vault from a repository")
        click(text = "Change…")
        click(text = "Import")

        expectText(
            text = "That is not the address of a repository Notekea can reach",
            substring = true
        )

        click(text = "https://github.com/you/notes.git")
        type(text = remote.toURI().toString())
        expectText(text = "import-remote")
        click(text = "Import")

        expectText(text = NOTE_NAME)
        expectFile(path = "import-remote/$NOTE_NAME.md")
        expectFolder(path = "import-remote/.git")
    }
}

/** A bare repository in the case's data folder that holds one note, pushed from a clone of it. */
private fun CaseScope.seededRemote(): File {
    val remote = file(path = REMOTE).toFile()
    val seed = file(path = SEED).toFile()

    Git
        .init()
        .setBare(true)
        .setDirectory(remote)
        .setInitialBranch("main")
        .call()
        .close()

    Git
        .cloneRepository()
        .setURI(remote.toURI().toString())
        .setDirectory(seed)
        .call()
        .use { git ->
            File(seed, "$NOTE_NAME.md").writeText("# $NOTE_NAME\n\nWritten on another computer.\n")

            git
                .add()
                .addFilepattern(".")
                .call()

            git
                .commit()
                .setMessage("first note")
                .setAuthor("Ana", "ana@example.com")
                .call()

            git
                .push()
                .setRemote("origin")
                .add("HEAD:refs/heads/main")
                .call()
        }

    return remote
}

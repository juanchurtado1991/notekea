package app.notekea.autotest.cases.m4

import app.notekea.autotest.cases.m1.PLAYGROUND
import com.testbuddy.SuiteBuilder
import java.io.RandomAccessFile
import java.nio.file.Files
import kotlin.time.Duration.Companion.minutes

private const val FILM = "Film.mp4"

// Past the 100 MB no Git host takes, written as a sparse file: it takes no room on the disk.
private const val FILM_BYTES = 101L * 1024 * 1024

/**
 * A file over 100 MB in a vault that syncs (the code review of 2026-11-22): it is left out of the repository, stays in
 * the vault, and a dialog names it.
 */
internal fun SuiteBuilder.largeFileCases() {
    automated(
        id = "4.2-08",
        title = "A file over 100 MB is left out of the repository, stays in the vault, and a dialog names it",
        timeout = 2.minutes
    ) {
        val remote = bareRemote()
        connect(remote = remote)
        clickIcon(label = "Close")

        RandomAccessFile(file(path = "$PLAYGROUND/$FILM").toFile(), "rw").use {
            it.setLength(FILM_BYTES)
        }

        // The commit comes ten seconds after this edit: the note reaching the repository says it was made.
        Files.writeString(file(path = "$PLAYGROUND/Next.md"), "# Next\n\nWritten beside the film.\n")

        expectInRemote(
            remote = remote,
            path = "Next.md"
        ) {
            it != null
        }

        expectText(text = "Files too large to sync")
        expectText(text = FILM)

        expectInRemote(
            remote = remote,
            path = FILM
        ) {
            it == null
        }

        click(text = "OK")
        expectNoText(text = "Files too large to sync")
        expectFile(path = "$PLAYGROUND/$FILM")
    }
}

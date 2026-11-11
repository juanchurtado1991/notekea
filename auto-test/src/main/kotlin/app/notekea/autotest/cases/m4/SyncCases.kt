package app.notekea.autotest.cases.m4

import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.openVault
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder
import org.eclipse.jgit.api.Git
import java.io.File
import java.nio.file.Files
import kotlin.time.Duration.Companion.minutes

private const val REMOTE = "remote.git"
private const val OTHER = "Other computer"
private const val ADDRESS_HINT = "https://github.com/you/notes.git"
private const val NOTE = "Welcome.md"
private const val UP_TO_DATE = "Up to date"
private const val REMOTE_WAIT_MILLIS = 40_000L
private const val POLL_MILLIS = 500L
private val SYNC_TIMEOUT = 2.minutes

/**
 * The cases of 4.2, Git sync (`MVP_SMOKE_TEST.md`): the Playground Vault synced with a repository on disk (a bare
 * one made by the case, `file://`), and the other computer played with a clone of it, so no account or network is
 * needed; GitHub with a token is the manual case.
 */
fun SuiteBuilder.syncCases() {
    connectAndCommit()
    conflict()
    gitHub()
}

private fun SuiteBuilder.connectAndCommit() {
    automated(
        id = "4.2-01",
        title = "Settings → Sync connects the vault to a repository, and an edit reaches it by itself",
        timeout = SYNC_TIMEOUT
    ) {
        val remote = bareRemote()
        connect(remote = remote)

        expectInRemote(
            remote = remote,
            path = NOTE
        ) {
            it != null
        }

        Files.writeString(file(path = "$PLAYGROUND/Synced.md"), "# Synced\n\nWritten on this computer.\n")

        expectInRemote(
            remote = remote,
            path = "Synced.md"
        ) {
            it?.contains("this computer") == true
        }

        expectText(text = UP_TO_DATE)
    }
}

private fun SuiteBuilder.conflict() {
    automated(
        id = "4.2-02",
        title = "A note changed on both computers shows a banner, both versions, and Keep mine settles it",
        timeout = SYNC_TIMEOUT
    ) {
        val remote = bareRemote()
        connect(remote = remote)

        expectInRemote(
            remote = remote,
            path = NOTE
        ) {
            it != null
        }

        changeOnOtherComputer(
            remote = remote,
            text = "# Welcome\n\nChanged on the other computer.\n"
        )

        Files.writeString(file(path = "$PLAYGROUND/$NOTE"), "# Welcome\n\nChanged on this computer.\n")
        click(text = "Sync now")

        expectText(text = "Changed here and on another computer: Welcome")
        clickIcon(label = "Close")
        expectNoText(text = "Stop syncing")
        click(text = "Review")
        expectText(text = "Welcome changed in two places")

        expectText(
            text = "Changed on this computer.",
            substring = true
        )

        expectText(
            text = "Changed on the other computer.",
            substring = true
        )

        click(text = "Keep mine")

        expectFile(
            path = "$PLAYGROUND/$NOTE",
            containing = "Changed on this computer."
        )

        expectNoFile(path = "$PLAYGROUND/Welcome (conflict).md")
        expectNoText(text = "Review")

        expectInRemote(
            remote = remote,
            path = NOTE
        ) {
            it?.contains("this computer") == true
        }
    }
}

private fun SuiteBuilder.gitHub() {
    manual(
        id = "4.2-03",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "The vault syncs with a private GitHub repository through a token, on two computers")

        steps(
            text =
            "1. On github.com make a private repository with no files, and a fine-grained personal access token " +
                "that can read and write its contents.\n2. In Notekea open the Playground Vault, click Settings " +
                "(the gear at the bottom of the narrow bar on the far left), then Sync; paste the repository's " +
                "HTTPS address and the token, and click Connect.\n3. Change a note, wait 15 seconds and look at " +
                "the repository on github.com.\n4. On a second computer (or a copy of Notekea QA with another " +
                "vault folder), connect an empty vault to the same repository with the token.\n5. Type a wrong " +
                "token in Settings → Sync and click Connect."
        )

        expected(
            text =
            "Step 2 says Up to date in the status bar and the repository holds the vault's notes. Step 3's change " +
                "is in the repository. Step 4 brings every note to the second vault. Step 5 says the repository " +
                "refused the token, and the status bar says Sync sign-in failed; the right token fixes it."
        )
    }
}

/** A bare repository in the case's data folder, the remote both computers share. */
private fun CaseScope.bareRemote(): File {
    val remote = file(path = REMOTE).toFile()

    Git
        .init()
        .setBare(true)
        .setDirectory(remote)
        .setInitialBranch("main")
        .call()
        .close()

    return remote
}

/** Opens the Playground Vault and connects it to [remote] in Settings → Sync. */
private fun CaseScope.connect(remote: File) {
    openVault(
        name = PLAYGROUND,
        shownText = WELCOME
    )

    clickIcon(label = "Settings")
    click(text = "Sync")
    click(text = ADDRESS_HINT)
    type(text = remote.toURI().toString())
    click(text = "Connect")
    expectText(text = "Stop syncing")
}

/** The other computer: a clone of [remote] whose Welcome note is changed to [text] and pushed. */
private fun CaseScope.changeOnOtherComputer(
    remote: File,
    text: String
) {
    val other = file(path = OTHER).toFile()

    Git
        .cloneRepository()
        .setURI(remote.toURI().toString())
        .setDirectory(other)
        .call()
        .use { git ->
            File(other, NOTE).writeText(text)

            git
                .add()
                .addFilepattern(".")
                .call()

            git
                .commit()
                .setMessage("other computer")
                .setAuthor("Ana", "ana@example.com")
                .call()

            git.push().call()
        }
}

/** Waits until the file at [path] on [remote]'s branch passes [check] (its text, null while it is not there). */
private fun CaseScope.expectInRemote(
    remote: File,
    path: String,
    check: (String?) -> Boolean
) {
    step(description = "Expect “$path” in the repository")
    var last: String? = null
    val deadline = System.currentTimeMillis() + REMOTE_WAIT_MILLIS

    while (System.currentTimeMillis() < deadline) {
        last = textInRemote(
            remote = remote,
            path = path
        )

        if (check(last)) return
        settle(millis = POLL_MILLIS)
    }

    throw CaseFailure(message = "expected the repository's \"$path\" to change, it holds: $last")
}

private fun textInRemote(
    remote: File,
    path: String
): String? = Git.open(remote).use { git ->
    val repository = git.repository
    val head = repository.resolve("refs/heads/main") ?: repository.resolve("HEAD") ?: return null

    org
        .eclipse
        .jgit
        .revwalk
        .RevWalk(repository)
        .use { walk ->
            val tree = walk.parseCommit(head).tree

            org
                .eclipse
                .jgit
                .treewalk
                .TreeWalk
                .forPath(repository, path, tree)
                ?.use { String(repository.open(it.getObjectId(0)).bytes) }
        }
}

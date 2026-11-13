package app.notekea.autotest.cases.m4

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasText
import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.cases.m3.MODEL_CASE_TIMEOUT
import app.notekea.autotest.cases.m3.awaitReply
import app.notekea.autotest.cases.m3.expectAnyText
import app.notekea.autotest.notekea
import app.notekea.autotest.openVault
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder
import org.eclipse.jgit.api.Git
import java.io.File
import java.io.RandomAccessFile
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
private const val HELP_FIELD = "Ask about a setting…"
private const val FIRST_STEP = "1. Make a free account at github.com."
private const val TOO_LARGE = "Large vault for GitHub"
private const val GITHUB_ADDRESS = "https://github.com/ana/notes.git"

// A vault past GitHub's 1 GB, written as a sparse file: it takes no room on the disk.
private const val LARGE_BYTES = 1_100_000_000L

/**
 * The cases of 4.2, Git sync (`MVP_SMOKE_TEST.md`): the Playground Vault synced with a repository on disk (a bare
 * one made by the case, `file://`), and the other computer played with a clone of it, so no account or network is
 * needed; GitHub with a token is the manual case.
 */
fun SuiteBuilder.syncCases() {
    connectAndCommit()
    conflict()
    gitHub()
    helpAndSize()
    helpAnswers()
    helpShows()
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

        title(
            text =
            "La bóveda se sincroniza con un repositorio privado de GitHub mediante un token, " +
                "en dos computadoras"
        )

        steps(
            text =
            "1. En github.com crea un repositorio privado sin archivos, y un token de acceso " +
                "personal fine-grained que pueda leer y escribir su contenido.\n2. En Notekea " +
                "abre la Playground Vault, haz clic en Ajustes (el engranaje abajo en la barra " +
                "estrecha de la izquierda) y luego en Sincronización; pega la dirección HTTPS " +
                "del repositorio y el token, y haz clic en Conectar.\n3. Cambia una nota, espera " +
                "15 segundos y mira el repositorio en github.com.\n4. En una segunda computadora " +
                "(o una copia de Notekea QA con otra carpeta de bóveda), conecta una bóveda " +
                "vacía al mismo repositorio con el token.\n5. Escribe un token incorrecto en " +
                "Ajustes → Sincronización y haz clic en Conectar."
        )

        expected(
            text =
            "El paso 2 dice Al día en la barra de estado y el repositorio tiene las notas de " +
                "la bóveda. El cambio del paso 3 está en el repositorio. El paso 4 trae todas " +
                "las notas a la segunda bóveda. El paso 5 dice que el repositorio rechazó el " +
                "token, y la barra de estado dice Falló el acceso de la sincronización; el token " +
                "correcto lo arregla."
        )
    }
}

// The developer's requests of 2026-11-11: most people do not know GitHub, so Ask about Settings beside the dialog
// shows the steps; a vault over 1 GB is warned of before GitHub only, never kept on this computer or synced elsewhere.
private fun SuiteBuilder.helpAndSize() {
    automated(
        id = "4.2-04",
        title = "Ask about Settings shows GitHub's steps beside Sync; a vault over 1 GB is warned of for GitHub only"
    ) {
        RandomAccessFile(file(path = "$PLAYGROUND/Trip.mp4").toFile(), "rw").use {
            it.setLength(LARGE_BYTES)
        }

        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Settings")
        expectText(text = "Ask about Settings")

        expectText(
            text = "Ask what any setting here does",
            substring = true
        )

        click(text = "Sync")

        expectText(
            text = FIRST_STEP,
            substring = true
        )

        // Kept on this computer, with no address typed: nothing is said (local remotes are in the unit tests).
        expectNoText(text = TOO_LARGE)
        click(text = ADDRESS_HINT)
        type(text = GITHUB_ADDRESS)
        expectText(text = TOO_LARGE)

        expectText(
            text = "This vault is 1.1 GB.",
            substring = true
        )
    }
}

private fun SuiteBuilder.helpAnswers() {
    automated(
        id = "4.2-05",
        title = "Ask about Settings answers a question on GitHub, beside Sync, in the question's language",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        notekea().useChatModel()
        openSync()
        clickIcon(label = HELP_FIELD)
        type(text = "¿qué es un token?")
        press(key = Key.Enter)
        awaitReply()
        expectAnyText("contraseña", "llave", "clave", "GitHub")
    }
}

// The developer's request of 2026-11-11: the chat opens the setting a question names and makes it blink, as an
// intent opens a screen; named in the question, it needs no model.
private fun SuiteBuilder.helpShows() {
    automated(
        id = "4.2-06",
        title = "A question in Ask about Settings opens the section of the setting it names, which Show shows again"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Settings")
        expectText(text = "Language")
        clickIcon(label = HELP_FIELD)
        type(text = "¿dónde pongo el token?")
        press(key = Key.Enter)
        expectText(text = "Access token")
        expectText(text = "Show Access token")
        expectNoText(text = "Language")
        click(text = "Appearance")
        click(text = "Show Access token")
        expectText(text = "Access token")
    }
}

/** Opens the Playground Vault and Settings → Sync. */
private fun CaseScope.openSync() {
    openVault(
        name = PLAYGROUND,
        shownText = WELCOME
    )

    clickIcon(label = "Settings")
    click(text = "Sync")
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
    openSync()
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

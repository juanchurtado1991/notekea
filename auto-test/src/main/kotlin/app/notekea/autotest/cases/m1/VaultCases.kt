package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import app.notekea.autotest.notekea
import app.notekea.autotest.openVault
import app.notekea.autotest.relaunch
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val OPEN_FOLDER = "Open folder…"
private const val OPEN_ANOTHER = "Open another vault"
private const val CREATE = "Create new vault"
private const val CREATE_CONFIRM = "Create vault"
private const val NO_NOTES = "This folder has no notes yet"
private const val NOT_FOUND = "Folder not found"

/** The cases of 1.3, opening a vault (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.vaultCases() {
    firstLaunch()
    vaultOpens()
    lastVaultReopens()
    recentVaults()
    openFolderShortcut()
    movedVault()
    createVault()
    badVaultName()
    lockedVault()
    createVaultByKeyboard()
}

private fun SuiteBuilder.firstLaunch() {
    automated(
        id = "1.3-01",
        title = "The first launch shows the welcome screen"
    ) {
        expectText(text = OPEN_FOLDER)

        listOf(CREATE, "Plain Markdown", "Works with Obsidian", "Local AI", "Private").forEach {
            expectText(text = it)
        }

        expectNoText(text = "Recent")
    }
}

private fun SuiteBuilder.vaultOpens() {
    automated(
        id = "1.3-02",
        title = "A vault opens with its notes, without hidden folders"
    ) {
        openVault(
            name = PERSONAL_NOTES,
            shownText = WELCOME
        )

        listOf("Ideas", "Journal", "Projects", "Recipes", "Travel", "attachments").forEach {
            expectText(text = it)
        }

        expectNoText(text = ".obsidian")
        expectNoText(text = ".trash")
        click(text = "attachments")
        expectText(text = "shopping.txt")
    }
}

private fun SuiteBuilder.lastVaultReopens() {
    automated(
        id = "1.3-03",
        title = "The last vault reopens at launch"
    ) {
        openVault(
            name = PERSONAL_NOTES,
            shownText = WELCOME
        )

        relaunch()
        expectText(text = WELCOME)
        expectNoText(text = OPEN_FOLDER)
    }
}

private fun SuiteBuilder.recentVaults() {
    automated(
        id = "1.3-04",
        title = "Recent vaults, the most recent first"
    ) {
        openVault(
            name = PERSONAL_NOTES,
            shownText = WELCOME
        )

        openAnother(
            name = "Work Obsidian",
            shownText = "Projects"
        )

        openAnother(
            name = "Empty Vault",
            shownText = NO_NOTES
        )

        clickIcon(label = OPEN_ANOTHER)
        expectText(text = "Just now")

        val order = listOf("Empty Vault", "Work Obsidian", PERSONAL_NOTES).map { top(text = it) }

        if (order != order.sorted()) {
            throw CaseFailure(
                message = "expected Empty Vault, Work Obsidian, Personal Notes in that order"
            )
        }
    }
}

private fun SuiteBuilder.openFolderShortcut() {
    manual(
        id = "1.3-05",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PERSONAL_NOTES)
        title(text = "Ctrl+O (Cmd+O) abre el selector de carpetas")

        steps(
            text =
            "1. En Notekea, si hay una bóveda abierta, haz clic en Abrir otra bóveda abajo a " +
                "la izquierda.\n2. Pulsa Ctrl+O (Cmd+O en macOS) y luego Cancelar en la ventana " +
                "que se abre.\n3. Abre cualquier bóveda de la carpeta Test data, haz clic dentro " +
                "de una nota y pulsa de nuevo Ctrl+O (Cmd+O)."
        )

        expected(
            text =
            "La ventana del sistema para elegir una carpeta se abre las dos veces. " +
                "Cancelarla no cambia nada."
        )
    }
}

private fun SuiteBuilder.movedVault() {
    automated(
        id = "1.3-06",
        title = "A moved vault can be located or removed"
    ) {
        openVault(
            name = PERSONAL_NOTES,
            shownText = WELCOME
        )

        openAnother(
            name = "Movable Vault",
            shownText = WELCOME
        )

        relaunch {
            Files.move(folders.data.resolve("Movable Vault"), folders.data.resolve("Moved Vault"))

            Files.move(
                folders.data.resolve(PERSONAL_NOTES),
                folders.data.resolve("Personal Notes moved")
            )
        }

        expectText(text = NOT_FOUND)

        ui
            .onAllNodes(matcher = hasText(text = "Remove"))
            .onFirst()
            .clickWhenStill()

        expectNoText(text = "Movable Vault")

        notekea().folderPicker.nextPick = folders
            .data
            .resolve("Personal Notes moved")
            .toString()

        click(text = "Locate…")
        expectText(text = WELCOME)
    }
}

private fun SuiteBuilder.createVault() {
    automated(
        id = "1.3-07",
        title = "Create new vault makes an empty vault in Documents"
    ) {
        click(text = CREATE)
        expectText(text = "My Notes")
        click(text = CREATE_CONFIRM)
        expectText(text = NO_NOTES)
        val created = notekea().home.resolve("Documents/My Notes")

        val contents = Files.list(created).use { files ->
            files.map { it.fileName.toString() }.toList()
        }

        if (contents.any {
                it != ".obsidian"
            }
        ) {
            throw CaseFailure(message = "expected My Notes to be empty, it holds $contents")
        }
    }
}

private fun SuiteBuilder.badVaultName() {
    automated(
        id = "1.3-08",
        title = "A vault name that cannot be used is explained"
    ) {
        click(text = CREATE)

        notekea().folderPicker.nextPick = folders
            .data
            .resolve("New Vault Location")
            .toString()

        click(text = "Change…")
        click(text = CREATE_CONFIRM)

        expectText(
            text = "already has files in it",
            substring = true
        )

        click(text = "My Notes")
        clearText()
        type(text = "a/b")
        click(text = CREATE_CONFIRM)

        expectText(
            text = "Enter a name that is not too long and has no characters like / : * ? or quotes."
        )

        click(text = "a/b")
        type(text = "c")

        expectNoText(
            text = "Enter a name that is not too long and has no characters like / : * ? or quotes."
        )
    }
}

private fun SuiteBuilder.lockedVault() {
    manual(
        id = "1.3-09",
        reason = ManualReason.COSTLY
    ) {
        prepare("Locked Vault")
        title(text = "Una bóveda cuya carpeta no se puede leer")

        steps(
            text =
            "1. En Notekea, abre la Locked Vault (mira DATOS DE PRUEBA arriba) y luego haz " +
                "clic en Abrir otra bóveda.\n2. Quítate el permiso de lectura de la carpeta " +
                "Locked Vault: en Windows, clic derecho › Propiedades › Seguridad › Editar, " +
                "selecciona tu usuario y marca Denegar en Lectura; en macOS, Obtener información " +
                "› Compartir y permisos, tu usuario en Sin acceso; en Linux, ejecuta chmod 000 " +
                "sobre ella.\n3. Haz clic en Locked Vault en Recientes.\n4. Haz clic en otra " +
                "bóveda de Recientes, pulsa Ctrl+O (Cmd+O en macOS) y elige la carpeta Locked " +
                "Vault.\n5. Devuelve el permiso de la misma forma."
        )

        expected(
            text =
            "Las dos veces un aviso nombra la carpeta y dice que revises sus permisos (en el " +
                "paso 4, sobre la bóveda abierta); Descartar lo oculta. Nada se cierra de golpe."
        )
    }
}

private fun SuiteBuilder.createVaultByKeyboard() {
    automated(
        id = "1.3-10",
        title = "Create new vault with the keyboard only"
    ) {
        click(text = CREATE)
        clearText()
        type(text = "Keyboard vault")
        press(key = Key.Escape)
        expectNoText(text = CREATE_CONFIRM)
        click(text = CREATE)
        press(key = Key.Enter)
        expectText(text = NO_NOTES)
    }
}

private fun CaseScope.openAnother(
    name: String,
    shownText: String
) {
    clickIcon(label = OPEN_ANOTHER)

    openVault(
        name = name,
        shownText = shownText
    )
}

private fun CaseScope.top(text: String): Float = ui
    .onAllNodes(matcher = hasText(text = text))
    .onFirst()
    .fetchSemanticsNode()
    .boundsInRoot
    .top

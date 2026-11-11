package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeTimeoutException
import app.notekea.autotest.SmokeScenarios
import app.notekea.autotest.clickInTree
import app.notekea.autotest.notekea
import app.notekea.autotest.openVault
import app.notekea.autotest.relaunch
import app.notekea.autotest.rightClickInTree
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.HostSystem
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val DELETE_NOTE = "Delete note"
private const val SAMPLE_VAULT = "Sample Vault"
private const val REVEAL_WAIT_MILLIS = 10_000L

/** The cases of 1.4 that change the tree: deleting, other apps, a large vault, the sidebar (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.treeChangeCases() {
    deleteNoteAndFolder()
    showInFiles()
    outsideChanges()
    largeVault()
    sidebarHides()
    vaultMovedWhileOpen()
    restoreFromTrash()
    noteIcons()
}

private fun SuiteBuilder.deleteNoteAndFolder() {
    automated(
        id = "1.4-07",
        title = "Deleting a note and a folder, after a question"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickInTree(text = "Travel")
        clickInTree(text = "Packing checklist")
        clickInTree(text = "Packing checklist")
        pressDelete()
        expectText(text = "Delete this note?")
        click(text = "Cancel")
        expectFile(path = "$PLAYGROUND/Travel/Packing checklist.md")
        clickInTree(text = "Packing checklist")
        pressDelete()
        click(text = DELETE_NOTE)
        expectNoFile(path = "$PLAYGROUND/Travel/Packing checklist.md")
        clickInTree(text = "Travel")
        pressDelete()
        expectText(text = "Delete this folder?")
        click(text = "Delete folder")
        expectNoFile(path = "$PLAYGROUND/Travel/Lisbon trip.md")
    }
}

private fun SuiteBuilder.showInFiles() {
    automated(
        id = "1.4-08",
        title = "Show in Files asks the file manager for the note, from the right click and from Shift+F10"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        // Each system names its file manager (Show in Finder, Show in File Explorer).
        val reveal = when (system) {
            HostSystem.MACOS -> "Show in Finder"
            HostSystem.WINDOWS -> "Show in File Explorer"
            HostSystem.LINUX -> "Show in Files"
        }

        rightClickInTree(text = WELCOME)
        click(text = reveal)
        awaitRevealed(times = 1)

        clickInTree(text = WELCOME)
        press(shortcut = Shift + Key.F10)
        click(text = reveal)
        awaitRevealed(times = 2)
    }
}

/** Waits until the case's file manager was asked [times] times for the Welcome note. */
private fun CaseScope.awaitRevealed(times: Int) {
    step(description = "Expect Welcome shown in the file manager $times time(s)")

    try {
        ui
            .waitUntil(timeoutMillis = REVEAL_WAIT_MILLIS) {
                notekea()
                    .fileRevealer
                    .revealed
                    .count {
                        it.name == "$WELCOME.md"
                    } >= times
            }
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = "expected Welcome shown $times time(s)").apply {
            initCause(exception)
        }
    }
}

private fun SuiteBuilder.outsideChanges() {
    automated(
        id = "1.4-09",
        title = "Changes by other apps show without a click"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickInTree(text = "Ideas")
        expectText(text = "Reading list")

        SmokeScenarios(
            smokeData = folders.data,
            scratch = folders.data
        ).outside()

        expectText(text = "Inbox")
        expectNoText(text = "Reading list")
    }
}

private fun SuiteBuilder.largeVault() {
    automated(
        id = "1.4-10",
        title = "A large vault opens quickly, and at once the second time"
    ) {
        openVault(
            name = "Ghost Serializer",
            shownText = "ghost-compiler",
            waitsForIndex = false
        )

        listOf("ghost-api", "ghost-ktor").forEach { expectText(text = it) }

        relaunch()
        expectText(text = "ghost-compiler")
    }
}

private fun SuiteBuilder.sidebarHides() {
    automated(
        id = "1.4-11",
        title = "The sidebar hides and shows, and stays as it was left"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        press(shortcut = Mod + Key.Backslash)
        expectNoText(text = "Recipes")
        press(shortcut = Mod + Key.Backslash)
        expectText(text = "Recipes")
        clickIcon(label = "Minimize sidebar")
        expectNoText(text = "Recipes")
        relaunch()
        expectNoText(text = "Recipes")
        clickIcon(label = "Files")
        expectText(text = "Recipes")
    }
}

private fun SuiteBuilder.vaultMovedWhileOpen() {
    automated(
        id = "1.4-12",
        title = "A vault moved while it is open is explained"
    ) {
        openVault(
            name = "Movable Vault",
            shownText = WELCOME
        )

        Files.move(folders.data.resolve("Movable Vault"), folders.data.resolve("Moved Vault"))

        expectText(
            text = "Notekea can’t find",
            substring = true
        )

        click(text = "Try again")

        expectText(
            text = "Notekea can’t find",
            substring = true
        )
    }
}

private fun SuiteBuilder.restoreFromTrash() {
    manual(
        id = "1.4-13",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Una nota eliminada vuelve desde la papelera del sistema")

        steps(
            text =
            "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba).\n2. " +
                "Elimina la nota Travel › Packing checklist (selecciónala y pulsa Suprimir, en " +
                "macOS Delete o Cmd+Delete, y luego Eliminar nota).\n3. Abre la papelera del " +
                "sistema (Papelera de reciclaje en Windows, Papelera en macOS y Linux) y " +
                "restáurala."
        )

        expected(
            text =
            "Packing checklist vuelve a estar bajo Travel en Notekea en menos de un segundo, " +
                "sin hacer clic en nada."
        )
    }
}

private fun SuiteBuilder.noteIcons() {
    manual(
        id = "1.4-14",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE_VAULT)
        title(text = "Home, las plantillas y las notas diarias tienen sus propios iconos")

        steps(
            text =
            "1. En Notekea, abre la Sample Vault (mira DATOS DE PRUEBA arriba).\n2. En la " +
                "lista de la izquierda, mira el icono de Home, abre la carpeta Templates y luego " +
                "Journal › Daily, y mira sus notas."
        )

        expected(
            text =
            "Home tiene una casa. Daily y Meeting, en Templates, tienen el icono de " +
                "plantilla en el color de su carpeta. Las notas de Journal › Daily tienen un " +
                "calendario en el color de Journal. Todas las demás notas tienen una página."
        )
    }
}

private fun CaseScope.pressDelete() {
    if (system == HostSystem.MACOS) {
        press(shortcut = Mod + Key.Backspace)
    } else {
        press(key = Key.Delete)
    }
}

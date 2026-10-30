package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import app.notekea.selftest.SmokeScenarios
import app.notekea.selftest.clickInTree
import app.notekea.selftest.openVault
import app.notekea.selftest.relaunch
import com.testbuddy.CaseScope
import com.testbuddy.HostSystem
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val DELETE_NOTE = "Delete note"

/** The cases of 1.4 that change the tree: deleting, other apps, a large vault, the sidebar (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.treeChangeCases() {
    deleteNoteAndFolder()
    showInFiles()
    outsideChanges()
    largeVault()
    sidebarHides()
    vaultMovedWhileOpen()
    restoreFromTrash()
}

private fun SuiteBuilder.deleteNoteAndFolder() {
    automated(id = "1.4-07", title = "Deleting a note and a folder, after a question") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
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
    manual(id = "1.4-08", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "Show in Files opens the file manager")
        steps(
            text = "1. In Notekea, open the Playground Vault from the Test data folder (Open folder…, after Open " +
                "another vault at the bottom left if a vault is open).\n2. Right-click a note in the list on the " +
                "left and choose Show in File Explorer (Windows), Show in Finder (macOS) or Show in Files " +
                "(Linux).\n3. Select another note and press Shift+F10, or the Menu key if your keyboard has one."
        )
        expected(
            text = "The file manager opens with the note selected (on Linux, at least its folder opens). The menu " +
                "shows each action's shortcut, and Shift+F10 opens it on the selected note."
        )
    }
}

private fun SuiteBuilder.outsideChanges() {
    automated(id = "1.4-09", title = "Changes by other apps show without a click") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = "Ideas")
        expectText(text = "Reading list")
        SmokeScenarios(smokeData = folders.data, scratch = folders.data).outside()
        expectText(text = "Inbox")
        expectNoText(text = "Reading list")
    }
}

private fun SuiteBuilder.largeVault() {
    automated(id = "1.4-10", title = "A large vault opens quickly, and at once the second time") {
        openVault(name = "Ghost Serializer", shownText = "ghost-compiler")
        listOf("ghost-api", "ghost-ktor").forEach { expectText(text = it) }
        relaunch()
        expectText(text = "ghost-compiler")
    }
}

private fun SuiteBuilder.sidebarHides() {
    automated(id = "1.4-11", title = "The sidebar hides and shows, and stays as it was left") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
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
    automated(id = "1.4-12", title = "A vault moved while it is open is explained") {
        openVault(name = "Movable Vault", shownText = WELCOME)
        Files.move(folders.data.resolve("Movable Vault"), folders.data.resolve("Moved Vault"))
        expectText(text = "Notekea can’t find", substring = true)
        click(text = "Try again")
        expectText(text = "Notekea can’t find", substring = true)
    }
}

private fun SuiteBuilder.restoreFromTrash() {
    manual(id = "1.4-13", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "A deleted note comes back from the system's trash")
        steps(
            text = "1. In Notekea, open the Playground Vault from the Test data folder (Open folder…, after Open " +
                "another vault at the bottom left if a vault is open).\n2. Delete the note Travel › Packing " +
                "checklist (select it and press Delete, Cmd+Backspace on macOS, then Delete note).\n3. Open the " +
                "system's trash (Recycle Bin on Windows, Trash on macOS and Linux) and restore it."
        )
        expected(text = "Packing checklist is back under Travel in Notekea within a second, without clicking anything.")
    }
}

private fun CaseScope.pressDelete() {
    if (system == HostSystem.MACOS) press(shortcut = Mod + Key.Backspace) else press(key = Key.Delete)
}

package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import app.notekea.selftest.notekea
import app.notekea.selftest.openVault
import app.notekea.selftest.relaunch
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
    automated(id = "1.3-01", title = "The first launch shows the welcome screen") {
        expectText(text = OPEN_FOLDER)
        listOf(CREATE, "Plain Markdown", "Works with Obsidian", "Local AI", "Private").forEach { expectText(text = it) }
        expectNoText(text = "Recent")
    }
}

private fun SuiteBuilder.vaultOpens() {
    automated(id = "1.3-02", title = "A vault opens with its notes, without hidden folders") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        listOf("Ideas", "Journal", "Projects", "Recipes", "Travel", "attachments").forEach { expectText(text = it) }
        expectNoText(text = ".obsidian")
        expectNoText(text = ".trash")
        click(text = "attachments")
        expectText(text = "shopping.txt")
    }
}

private fun SuiteBuilder.lastVaultReopens() {
    automated(id = "1.3-03", title = "The last vault reopens at launch") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        relaunch()
        expectText(text = WELCOME)
        expectNoText(text = OPEN_FOLDER)
    }
}

private fun SuiteBuilder.recentVaults() {
    automated(id = "1.3-04", title = "Recent vaults, the most recent first") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        openAnother(name = "Work Obsidian", shownText = "Projects")
        openAnother(name = "Empty Vault", shownText = NO_NOTES)
        clickIcon(label = OPEN_ANOTHER)
        expectText(text = "Just now")
        val order = listOf("Empty Vault", "Work Obsidian", PERSONAL_NOTES).map { top(text = it) }
        if (order !=
            order.sorted()
        ) {
            throw CaseFailure(message = "expected Empty Vault, Work Obsidian, Personal Notes in that order")
        }
    }
}

private fun SuiteBuilder.openFolderShortcut() {
    manual(id = "1.3-05", reason = ManualReason.SYSTEM) {
        prepare(PERSONAL_NOTES)
        title(text = "Ctrl+O (Cmd+O) opens the folder picker")
        steps(
            text = "1. In Notekea, if a vault is open, click Open another vault at the bottom left.\n" +
                "2. Press Ctrl+O (Cmd+O on macOS), then press Cancel in the window that opens.\n" +
                "3. Open any vault from the Test data folder, click inside a note, and press Ctrl+O (Cmd+O) again."
        )
        expected(text = "The system's window for choosing a folder opens both times. Cancelling it changes nothing.")
    }
}

private fun SuiteBuilder.movedVault() {
    automated(id = "1.3-06", title = "A moved vault can be located or removed") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        openAnother(name = "Movable Vault", shownText = WELCOME)
        relaunch {
            Files.move(folders.data.resolve("Movable Vault"), folders.data.resolve("Moved Vault"))
            Files.move(folders.data.resolve(PERSONAL_NOTES), folders.data.resolve("Personal Notes moved"))
        }
        expectText(text = NOT_FOUND)
        ui.onAllNodes(matcher = hasText(text = "Remove")).onFirst().performClick()
        expectNoText(text = "Movable Vault")
        notekea().folderPicker.nextPick = folders.data.resolve("Personal Notes moved").toString()
        click(text = "Locate…")
        expectText(text = WELCOME)
    }
}

private fun SuiteBuilder.createVault() {
    automated(id = "1.3-07", title = "Create new vault makes an empty vault in Documents") {
        click(text = CREATE)
        expectText(text = "My Notes")
        click(text = CREATE_CONFIRM)
        expectText(text = NO_NOTES)
        val created = notekea().home.resolve("Documents/My Notes")
        val contents = Files.list(created).use { files -> files.map { it.fileName.toString() }.toList() }
        if (contents.any {
                it != ".obsidian"
            }
        ) {
            throw CaseFailure(message = "expected My Notes to be empty, it holds $contents")
        }
    }
}

private fun SuiteBuilder.badVaultName() {
    automated(id = "1.3-08", title = "A vault name that cannot be used is explained") {
        click(text = CREATE)
        notekea().folderPicker.nextPick = folders.data.resolve("New Vault Location").toString()
        click(text = "Change…")
        click(text = CREATE_CONFIRM)
        expectText(text = "already has files in it", substring = true)
        click(text = "My Notes")
        clearText()
        type(text = "a/b")
        click(text = CREATE_CONFIRM)
        expectText(text = "Enter a name without characters like / : * ? or quotes.")
        click(text = "a/b")
        type(text = "c")
        expectNoText(text = "Enter a name without characters like / : * ? or quotes.")
    }
}

private fun SuiteBuilder.lockedVault() {
    manual(id = "1.3-09", reason = ManualReason.COSTLY) {
        prepare("Locked Vault")
        title(text = "A vault whose folder cannot be read")
        steps(
            text =
            "1. In Notekea, open the Locked Vault of the Test data folder (Open folder…, after Open another vault at the bottom left if a vault is open), then click Open another " +
                "vault.\n2. Take away your permission to read the Locked Vault folder: on Windows, right-click it › " +
                "Properties › Security › Edit, select your user and check Deny for Read; on macOS, Get Info › " +
                "Sharing & Permissions, " +
                "your user to No Access; on Linux, run chmod 000 on it.\n3. Click Locked Vault in Recent.\n" +
                "4. Give the permission back the same way."
        )
        expected(
            text = "A banner names the folder and says to check its permissions; Dismiss hides it. Nothing crashes."
        )
    }
}

private fun SuiteBuilder.createVaultByKeyboard() {
    automated(id = "1.3-10", title = "Create new vault with the keyboard only") {
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
    openVault(name = name, shownText = shownText)
}

private fun CaseScope.top(text: String): Float =
    ui.onAllNodes(matcher = hasText(text = text)).onFirst().fetchSemanticsNode().boundsInRoot.top

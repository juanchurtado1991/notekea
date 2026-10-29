package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import app.notekea.selftest.clickInTree
import app.notekea.selftest.expectFolder
import app.notekea.selftest.renameInTree
import app.notekea.selftest.rightClickInTree
import app.notekea.selftest.treeRow
import app.notekea.selftest.openVault
import app.notekea.selftest.relaunch
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.HostSystem
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder

private const val RECIPES = "Recipes"

/** The cases of 1.4 that browse the tree: opening, the keyboard, sorting and making notes (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.treeCases() {
    treeOpensTabs()
    treeKeyboard()
    sortByDate()
    newNotesAndFolders()
    renameInTheTree()
    renameUpdatesLinks()
}

private fun SuiteBuilder.treeOpensTabs() {
    automated(id = "1.4-01", title = "Clicking the tree opens notes and folders in tabs") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        click(text = "Projects")
        click(text = "Garden")
        click(text = "Tomato plan")
        expectText(text = "Three levels deep on purpose", substring = true)
        click(text = RECIPES)
        click(text = "Arepas")
        listOf("#recipe", "#venezuelan", "Arepa", "Corn cakes").forEach { expectText(text = it) }
        click(text = RECIPES)
        expectText(text = "3 files")
    }
}

private fun SuiteBuilder.treeKeyboard() {
    automated(id = "1.4-02", title = "The tree with the keyboard") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        click(text = "Journal")
        expectText(text = "2026-10-21")
        press(key = Key.DirectionDown)
        pressOpen()
        expectText(text = "Planted the last tomato seedlings", substring = true)
        press(key = Key.DirectionLeft)
        press(key = Key.DirectionLeft)
        expectNoText(text = "2026-10-21")
    }
}

private fun SuiteBuilder.sortByDate() {
    automated(id = "1.4-03", title = "Sorting by date modified, remembered after a relaunch") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = RECIPES)
        clickIcon(label = "Sort notes: name")
        click(text = "Date modified, newest first")
        expectOrder(texts = listOf("Pasta e fagioli", "Arepas", "Café con leche"))
        relaunch()
        expectOrder(texts = listOf("Pasta e fagioli", "Arepas", "Café con leche"))
        clickIcon(label = "Sort notes: date modified")
        click(text = "Name")
        expectOrder(texts = listOf("Arepas", "Café con leche", "Pasta e fagioli"))
    }
}

private fun SuiteBuilder.newNotesAndFolders() {
    automated(id = "1.4-04", title = "New notes and folders where they were asked for") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = RECIPES)
        press(shortcut = Mod + Key.N)
        expectFile(path = "$PLAYGROUND/$RECIPES/Untitled.md")
        press(key = Key.Enter)
        rightClickInTree(text = RECIPES)
        expectText(text = "New note from template")
        click(text = "New folder")
        expectFolder(path = "$PLAYGROUND/$RECIPES/Untitled folder")
        press(key = Key.Enter)
        rightClickInTree(text = "Travel")
        click(text = "New note")
        expectFile(path = "$PLAYGROUND/Travel/Untitled.md")
    }
}

private fun SuiteBuilder.renameInTheTree() {
    automated(id = "1.4-05", title = "Renaming in the tree, and names that cannot be used") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        // A folder keeps the keyboard in the tree when clicked (a note gives it to the editor), so F2 (Enter on
        // macOS) reaches the tree.
        clickInTree(text = "Ideas")
        pressRename()
        type(text = "Ideas and plans")
        press(key = Key.Enter)
        expectFolder(path = "$PLAYGROUND/Ideas and plans")
        renameInTree(name = WELCOME, newName = "Good morning")
        expectFile(path = "$PLAYGROUND/Good morning.md")
        expectNoFile(path = "$PLAYGROUND/Welcome.md")
        clickInTree(text = "Travel")
        refusedName(name = "a/b", message = "Enter a name without characters like / : * ? or quotes.")
        refusedName(name = ".hidden", message = "Names can’t start with a dot, or the note would be hidden.")
        refusedName(name = "Packing checklist", message = "There’s already something named Packing checklist")
        press(key = Key.Escape)
        expectFile(path = "$PLAYGROUND/Travel/Lisbon trip.md")
    }
}

private fun SuiteBuilder.renameUpdatesLinks() {
    automated(id = "1.4-06", title = "Renaming a note updates the links to it") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = RECIPES)
        renameInTree(name = "Arepas", newName = "Arepas reina")
        expectText(text = "Links updated in 3 notes", substring = true)
        listOf("Welcome.md", "Journal/2026-10-20.md", "Editor/Read mode.md").forEach {
            expectFile(path = "$PLAYGROUND/$it", containing = "[[Arepas reina")
        }
    }
}

// Enter opens on Windows and Linux; on macOS it renames, as in Finder, and Cmd+Down opens.
private fun CaseScope.pressOpen() {
    if (system == HostSystem.MACOS) press(shortcut = Mod + Key.DirectionDown) else press(key = Key.Enter)
}

private fun CaseScope.pressRename() {
    if (system == HostSystem.MACOS) press(key = Key.Enter) else press(key = Key.F2)
}

private fun CaseScope.refusedName(
    name: String,
    message: String
) {
    renameInTree(name = "Lisbon trip", newName = name)
    expectText(text = message, substring = true)
    press(key = Key.Escape)
}

/** Fails unless [texts] show from top to bottom in that order. */
internal fun CaseScope.expectOrder(texts: List<String>) {
    texts.forEach { expectText(text = it) }
    val tops = texts.map { text -> treeRow(text = text).fetchSemanticsNode().boundsInRoot.top }
    if (tops != tops.sorted()) throw CaseFailure(message = "expected ${texts.joinToString()} in that order")
}

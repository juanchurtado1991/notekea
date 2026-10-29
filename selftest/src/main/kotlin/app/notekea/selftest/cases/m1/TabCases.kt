package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import app.notekea.selftest.clickInTree
import app.notekea.selftest.noteText
import app.notekea.selftest.openNote
import app.notekea.selftest.openVault
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.relaunch
import app.notekea.selftest.treeRow
import com.testbuddy.isShown
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Ctrl
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val NESTED_NOTE = "Broken properties"
private const val PASTA_TEXT = "Add tomatoes"
private const val CAFE_TEXT = "Half strong coffee"
private const val WELCOME_TEXT = "This is a sample vault"

/** The cases of 1.11 about the tool rail, the sidebar's header and the tabs of one pane (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.tabCases() {
    toolRail()
    sidebarHeader()
    clicksOpenTabs()
    tabsBehind()
    closingTabs()
    tabShortcuts()
}

private fun SuiteBuilder.toolRail() {
    automated(id = "1.11-01", title = "The tool rail stays, and every icon works with the sidebar hidden") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        listOf("Files", "Search notes", "Today", "Side panel", "Open another vault", "About Notekea", "Settings")
            .forEach { expectIcon(label = it) }
        clickIcon(label = "Minimize sidebar")
        expectNoText(text = "Recipes")
        clickIcon(label = "Search notes")
        expectText(text = "Find a note by name or alias")
        press(key = Key.Escape)
        clickIcon(label = "Files")
        expectText(text = "Recipes")
    }
}

private fun SuiteBuilder.sidebarHeader() {
    automated(id = "1.11-02", title = "Expand all and Collapse all, and the open folders after a relaunch") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickIcon(label = "Expand all folders")
        expectText(text = NESTED_NOTE)
        clickIcon(label = "Collapse all folders")
        expectNoText(text = NESTED_NOTE)
        clickIcon(label = "Expand all folders")
        expectText(text = NESTED_NOTE)
        relaunch()
        expectText(text = NESTED_NOTE)
    }
}

private fun SuiteBuilder.clicksOpenTabs() {
    automated(id = "1.11-03", title = "Each click in the tree opens a tab, or goes to the open one") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = WELCOME)
        clickInTree(text = "Recipes")
        clickInTree(text = "Arepas")
        clickInTree(text = "Editor")
        clickInTree(text = WELCOME)
        listOf(WELCOME, "Arepas", "Editor").forEach { tab(name = it) }
        expectTabOrder(names = listOf(WELCOME, "Recipes", "Arepas", "Editor"))
        if (tabsShowing(name = WELCOME).size != 1) throw CaseFailure(message = "expected one Welcome tab")
    }
}

private fun SuiteBuilder.tabsBehind() {
    automated(id = "1.11-04", title = "Mod+click and the middle button open tabs behind the shown one") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = WELCOME)
        expandInTree(name = "Recipes")
        clickInTree(text = WELCOME)
        modClick(node = treeRow(text = "Pasta e fagioli"))
        tab(name = "Pasta e fagioli")
        middleClick(node = treeRow(text = "Café con leche"))
        tab(name = "Café con leche")
        expectShown(text = WELCOME_TEXT)
        expectNoText(text = PASTA_TEXT, substring = true)
        expectNoText(text = CAFE_TEXT, substring = true)
    }
}

private fun SuiteBuilder.closingTabs() {
    automated(id = "1.11-05", title = "Closing tabs with ✕, the middle button, Mod+W and Close other tabs") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = "Recipes")
        listOf("Arepas", "Café con leche", "Pasta e fagioli", WELCOME).forEach { clickInTree(text = it) }
        closeTabWithX(name = "Arepas")
        expectNoTab(name = "Arepas")
        middleClick(node = tab(name = "Café con leche"))
        expectNoTab(name = "Café con leche")
        pressInWindow(shortcut = Mod + Key.W)
        expectNoTab(name = WELCOME)
        clickInTree(text = "Arepas")
        rightClickTab(name = "Arepas")
        click(text = "Close other tabs")
        expectNoTab(name = "Pasta e fagioli")
        pressInWindow(shortcut = Mod + Key.W)
        expectText(text = "No open tabs")
        listOf("Find a note", "New note", "Today’s note", "Search in notes").forEach { expectText(text = it) }
    }
}

private fun SuiteBuilder.tabShortcuts() {
    automated(id = "1.11-06", title = "Ctrl+Tab and Mod+1…9 move between tabs, also while typing") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        expandInTree(name = "Recipes")
        listOf(WELCOME, "Arepas", "Café con leche", "Pasta e fagioli").forEach { clickInTree(text = it) }
        pressInWindow(shortcut = Ctrl + Key.Tab)
        expectShown(text = WELCOME_TEXT)
        pressInWindow(shortcut = Ctrl + Shift + Key.Tab)
        expectShown(text = PASTA_TEXT)
        pressInWindow(shortcut = Mod + Key.Two)
        expectShown(text = AREPAS_FLOUR)
        pressInWindow(shortcut = Mod + Key.Nine)
        expectShown(text = PASTA_TEXT)
        noteText(part = PASTA_TEXT).performClick()
        press(shortcut = Ctrl + Key.Tab)
        expectShown(text = WELCOME_TEXT)
    }
}

private fun CaseScope.expectIcon(label: String) {
    ui.waitUntil(timeoutMillis = 10_000) {
        ui.onAllNodes(matcher = hasContentDescription(value = label)).fetchSemanticsNodes().isNotEmpty()
    }
}

/** Waits until the open note shows [text]: the note's field holds it and is on screen. */
internal fun CaseScope.expectShown(text: String) {
    ui.waitUntil(timeoutMillis = 10_000) {
        val nodes = ui.onAllNodes(matcher = hasSetTextAction() and hasText(text = text, substring = true))
        val count = nodes.fetchSemanticsNodes().size
        (0 until count).any { nodes[it].isShown() }
    }
}

package app.notekea.selftest.cases.m1

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.rightClick
import app.notekea.selftest.SmokeScenarios
import app.notekea.selftest.clickInTree
import app.notekea.selftest.openVault
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.relaunch
import app.notekea.selftest.titleField
import com.testbuddy.isShown
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.HostSystem
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder
import java.nio.file.Files
import java.nio.file.Path

// Section headers are written in capitals.
private const val BOOKMARKS = "BOOKMARKS"
private const val PLATE = "Arepas plate.png"
private const val DRAG_STEPS = 10

/** The cases of 1.13, bookmarks (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.bookmarkCases() {
    starBookmarks()
    menuBookmarks()
    bookmarksOpen()
    bookmarksReorder()
    bookmarksFollow()
    missingBookmark()
    bookmarksStayOut()
}

private fun SuiteBuilder.starBookmarks() {
    automated(id = "1.13-01", title = "The star bookmarks the note, and a second click removes it") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = WELCOME)
        clickIcon(label = "Bookmark this note")
        expectText(text = BOOKMARKS)
        clickIcon(label = "Remove the bookmark")
        expectNoText(text = BOOKMARKS)
        clickIcon(label = "Bookmark this note")
        expectText(text = BOOKMARKS)
    }
}

private fun SuiteBuilder.menuBookmarks() {
    automated(id = "1.13-02", title = "Folders, files and the open note are bookmarked in that order") {
        bookmarkFour()
        expectBookmarkOrder(names = listOf(WELCOME, "Recipes", PLATE, "Ideas"))
        lowest(name = "Recipes").performMouseInput { this.rightClick() }
        expectText(text = "Remove bookmark")
        press(key = Key.Escape)
    }
}

private fun SuiteBuilder.bookmarksOpen() {
    automated(
        id = "1.13-03",
        title = "A bookmark opens its file with its folders open, and the header folds the list"
    ) {
        bookmarkFour()
        clickIcon(label = "Collapse all folders")
        highest(name = PLATE).performClick()
        expectText(text = "Image · 480 × 240", substring = true)
        expectText(text = "Weekly menu.pdf")
        highest(name = "Recipes").performClick()
        expectText(text = "3 files")
        // The plate shows twice in the sidebar, as a bookmark and in the tree; folded, only the tree's is left.
        click(text = BOOKMARKS)
        ui.waitUntil(timeoutMillis = 10_000) { shownCount(name = PLATE) == 1 }
        click(text = BOOKMARKS)
        ui.waitUntil(timeoutMillis = 10_000) { shownCount(name = PLATE) == 2 }
    }
}

private fun SuiteBuilder.bookmarksReorder() {
    automated(id = "1.13-04", title = "Bookmarks are reordered by dragging, per vault, kept after a relaunch") {
        bookmarkFour()
        val welcomeTop = highest(name = WELCOME).fetchSemanticsNode().boundsInRoot.top
        val ideas = highest(name = "Ideas")
        val dy = welcomeTop - ideas.fetchSemanticsNode().boundsInRoot.center.y + DRAG_MARGIN
        ideas.performMouseInput {
            moveTo(position = center)
            press()
            repeat(times = DRAG_STEPS) { moveBy(delta = Offset(x = 0f, y = dy / DRAG_STEPS)) }
            release()
        }
        expectBookmarkOrder(names = listOf("Ideas", WELCOME, "Recipes", PLATE))
        relaunch()
        expectBookmarkOrder(names = listOf("Ideas", WELCOME, "Recipes", PLATE))
        clickIcon(label = "Open another vault")
        openVault(name = "Work Obsidian", shownText = "Projects")
        expectNoText(text = BOOKMARKS)
    }
}

private fun SuiteBuilder.bookmarksFollow() {
    automated(id = "1.13-05", title = "Bookmarks follow renames and go with what is deleted") {
        bookmarkFour()
        lowest(name = "Recipes").performMouseInput { this.rightClick() }
        click(text = "Rename")
        type(text = "Cooking")
        press(key = Key.Enter)
        expectBookmarkOrder(names = listOf(WELCOME, "Cooking", PLATE, "Ideas"))
        clickInTree(text = WELCOME)
        titleField(name = WELCOME).apply {
            performClick()
            performTextReplacement(text = "Start here")
        }
        titleField(name = "Start here").performKeyInput { pressKey(key = Key.Enter) }
        expectBookmarkOrder(names = listOf("Start here", "Cooking", PLATE, "Ideas"))
        lowest(name = "Ideas").performClick()
        if (system == HostSystem.MACOS) press(shortcut = Mod + Key.Backspace) else press(key = Key.Delete)
        click(text = "Delete folder")
        ui.waitUntil(timeoutMillis = 10_000) { shownCount(name = "Ideas") == 0 }
        expectBookmarkOrder(names = listOf("Start here", "Cooking", PLATE))
    }
}

private fun SuiteBuilder.missingBookmark() {
    automated(id = "1.13-06", title = "A bookmark of a deleted note is greyed out and can be removed") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = "Editor")
        clickInTree(text = "Shared note")
        clickIcon(label = "Bookmark this note")
        expectText(text = BOOKMARKS)
        relaunch { SmokeScenarios(smokeData = folders.data, scratch = folders.data).deleteNote() }
        clickIcon(label = "Remove: it is no longer in the vault")
        expectNoText(text = BOOKMARKS)
    }
}

private fun SuiteBuilder.bookmarksStayOut() {
    automated(id = "1.13-07", title = "Bookmarks write nothing into the vault") {
        val before = snapshot(folder = folders.data.resolve(PLAYGROUND))
        bookmarkFour()
        relaunch()
        expectText(text = BOOKMARKS)
        val after = snapshot(folder = folders.data.resolve(PLAYGROUND))
        if (after != before) throw CaseFailure(message = "expected the vault unchanged, ${after - before} changed")
    }
}

/** Opens the Playground Vault and bookmarks Welcome, Recipes, Arepas plate.png and Ideas, as 1.13-02 does. */
private fun CaseScope.bookmarkFour() {
    openVault(name = PLAYGROUND, shownText = WELCOME)
    clickInTree(text = WELCOME)
    clickIcon(label = "Bookmark this note")
    bookmarkFromMenu(name = "Recipes")
    clickInTree(text = "attachments")
    bookmarkFromMenu(name = PLATE)
    clickInTree(text = "Ideas")
    // The shortcut bookmarks what is open: wait until the Ideas folder is, or it goes to the tab before.
    expectText(text = "2 files")
    pressInWindow(shortcut = Mod + Shift + Key.B)
    expectBookmarkOrder(names = listOf(WELCOME, "Recipes", PLATE, "Ideas"))
}

private fun CaseScope.bookmarkFromMenu(name: String) {
    lowest(name = name).performMouseInput { this.rightClick() }
    click(text = "Bookmark")
}

/** The bookmarks list sits above the tree, so a bookmark is the highest row showing its name, the tree's the lowest. */
private fun CaseScope.highest(name: String): SemanticsNodeInteraction = shown(name = name).minBy { it.top() }

private fun CaseScope.lowest(name: String): SemanticsNodeInteraction = shown(name = name).maxBy { it.top() }

// The tree is drawn anew after a rename, so its rows can be gone for a moment: this waits until they are back.
private fun CaseScope.shown(name: String): List<SemanticsNodeInteraction> {
    expectText(text = name)
    var rows = emptyList<SemanticsNodeInteraction>()
    ui.waitUntil(timeoutMillis = 10_000) {
        rows = shownNow(name = name)
        rows.isNotEmpty()
    }
    return rows
}

// No waiting inside: it runs within the waits of expectBookmarkOrder, which cannot hold other waits.
private fun CaseScope.shownNow(name: String): List<SemanticsNodeInteraction> {
    val nodes = ui.onAllNodes(matcher = hasText(text = name))
    val count = nodes.fetchSemanticsNodes().size
    return (0 until count).map { nodes[it] }.filter {
        it.isShown() &&
            it.fetchSemanticsNode().boundsInRoot.left < SIDEBAR_RIGHT
    }
}

private fun CaseScope.shownCount(name: String): Int {
    val nodes = ui.onAllNodes(matcher = hasText(text = name))
    val count = nodes.fetchSemanticsNodes().size
    return (0 until count).count {
        nodes[it].isShown() &&
            nodes[it].fetchSemanticsNode().boundsInRoot.left < SIDEBAR_RIGHT
    }
}

private fun CaseScope.expectBookmarkOrder(names: List<String>) {
    ui.waitUntil(timeoutMillis = 10_000) {
        val tops = names.map { name -> shownNow(name = name).minOfOrNull { it.top() } ?: Float.MAX_VALUE }
        tops.none { it == Float.MAX_VALUE } && tops == tops.sorted()
    }
}

private fun SemanticsNodeInteraction.top(): Float = fetchSemanticsNode().boundsInRoot.top

private fun snapshot(folder: Path): Set<String> = Files.walk(folder).use { paths ->
    paths.filter { Files.isRegularFile(it) }.map { "${folder.relativize(it)} ${Files.size(it)}" }.toList().toSet()
}

// The sidebar ends near x = 385; the tab strip starts right after it and holds the same names.
private const val SIDEBAR_RIGHT = 370f
private const val DRAG_MARGIN = -6f

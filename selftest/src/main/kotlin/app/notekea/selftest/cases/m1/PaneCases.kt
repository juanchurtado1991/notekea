package app.notekea.selftest.cases.m1

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import app.notekea.selftest.SmokeScenarios
import app.notekea.selftest.clickInTree
import app.notekea.selftest.relaunch
import app.notekea.selftest.noteText
import app.notekea.selftest.openNote
import app.notekea.selftest.openVault
import app.notekea.selftest.pressInWindow
import com.testbuddy.isShown
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription

private const val WELCOME_TEXT = "This is a sample vault"
private const val SHARED_NOTE = "Shared note"
private const val SHARED_NOTE_TEXT = "Another app changes or deletes this note"

// A heading in the middle of the note, as the case says: the end of a note is where the scroll stops short.
private const val OUTLINE_MIDDLE = "Serving note 1:"
private const val OPEN_PLAYGROUND = "1. In Notekea, open the Playground Vault (see TEST DATA above)."

/** The cases of 1.11 about two panes, the tabs' memory and the folders' cards (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.paneCases() {
    splitRight()
    tabBetweenPanes()
    placeKept()
    sessionKept()
    folderCards()
    nativeTitleBar()
    windowSize()
    quickFolderClicks()
    reorderTabs()
    tabSlides()
}

private fun SuiteBuilder.splitRight() {
    automated(id = "1.11-07", title = "Mod+Shift+D moves the tab to a pane on the right") {
        openTwoNotes()
        pressInWindow(shortcut = Mod + Shift + Key.D)
        expectShown(text = WELCOME_TEXT)
        expectShown(text = AREPAS_FLOUR)
        if (tabLeft(name = "Arepas") <
            tabLeft(name = WELCOME)
        ) {
            throw CaseFailure(message = "expected Arepas in the right pane")
        }
        pressInWindow(shortcut = Mod + Shift + Key.D)
        expectShown(text = WELCOME_TEXT)
        rightClickTab(name = WELCOME)
        expectText(text = "Move to the other pane")
        expectNoText(text = "Split right")
        press(key = Key.Escape)
        noteText(part = WELCOME_TEXT).performClick()
        expectNoText(text = "Ingredients")
    }
}

private fun SuiteBuilder.tabBetweenPanes() {
    automated(id = "1.11-08", title = "A tab dragged to the other pane moves there, and an empty pane closes") {
        openTwoNotes()
        pressInWindow(shortcut = Mod + Shift + Key.D)
        expectShown(text = AREPAS_FLOUR)
        val target = tab(name = "Arepas").fetchSemanticsNode().boundsInRoot
        dragTab(name = WELCOME, by = Offset(x = target.right + DROP_MARGIN - tabCenterX(name = WELCOME), y = 0f))
        expectTabOrder(names = listOf("Arepas", WELCOME))
        expectShown(text = WELCOME_TEXT)
        expectNoText(text = AREPAS_FLOUR, substring = true)
    }
}

private fun SuiteBuilder.placeKept() {
    automated(id = "1.11-09", title = "A tab shows the same part of its note when it comes back") {
        openPlaygroundNote(name = "Outline practice", shownText = "Arepas guide")
        pressInWindow(shortcut = Mod + Key.E)
        clickInOutline(heading = "Serving")
        expectOnScreen(text = OUTLINE_MIDDLE)
        clickInTree(text = WELCOME)
        tab(name = "Outline practice").performClick()
        expectOnScreen(text = OUTLINE_MIDDLE)
    }
}

private fun SuiteBuilder.sessionKept() {
    automated(id = "1.11-10", title = "The tabs and the split come back, without a note deleted while closed") {
        openTwoNotes()
        pressInWindow(shortcut = Mod + Shift + Key.D)
        expectShown(text = AREPAS_FLOUR)
        relaunch()
        expectShown(text = WELCOME_TEXT)
        expectShown(text = AREPAS_FLOUR)
        openNote(name = SHARED_NOTE, shownText = SHARED_NOTE_TEXT)
        relaunch { SmokeScenarios(smokeData = folders.data, scratch = folders.data).deleteNote() }
        expectShown(text = AREPAS_FLOUR)
        expectNoTab(name = SHARED_NOTE)
        expectNoText(text = "couldn't be opened", substring = true)
    }
}

private fun SuiteBuilder.folderCards() {
    automated(id = "1.11-11", title = "A folder opens as a grid of cards that open what they show") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = "Editor")
        listOf("Embeds", "Images and files", "Read mode").forEach { card(name = it) }
        if (card(name = "Embeds").top() > card(name = "Read mode").top()) {
            throw CaseFailure(message = "expected the subfolder Embeds before the notes")
        }
        card(name = "Read mode").performClick()
        expectShown(text = "Some **bold**")
        clickInTree(text = "attachments")
        listOf("Arepas plate.png", "Market photo.jpg", "broken.png").forEach { card(name = it) }
    }
}

private fun SuiteBuilder.nativeTitleBar() {
    manual(id = "1.11-12", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "The tabs sit under the system's own title bar")
        steps(text = "$OPEN_PLAYGROUND\n2. Open a few notes and look at the top of the window.")
        expected(
            text = "The tabs are inside the window, under its title bar; the title bar and its buttons are your " +
                "system's own."
        )
    }
}

private fun SuiteBuilder.windowSize() {
    manual(id = "1.11-13", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "The window keeps a sensible size")
        steps(
            text = "$OPEN_PLAYGROUND\n2. Quit Notekea and open it again.\n3. Click Open another vault at the bottom " +
                "left and look at the window, then open the Playground Vault from Recent. Do this twice more.\n4. " +
                "Resize the window and quit and open Notekea once more."
        )
        expected(
            text = "The window opens centered, never larger than the screen, showing the tools, the list of notes, a " +
                "note and the outline. It keeps its size when a vault opens or closes, and the size you gave it " +
                "stays."
        )
    }
}

private fun SuiteBuilder.quickFolderClicks() {
    automated(id = "1.11-14", title = "Opening and closing folders quickly never shows an error") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickIcon(label = "Collapse all folders")
        repeat(times = QUICK_CLICKS) { clickInTree(text = if (it % 2 == 0) "Recipes" else "Travel") }
        expectNoText(text = "Something went wrong. Please try again.")
        expectNoText(text = "Something went wrong", substring = true)
    }
}

private fun SuiteBuilder.reorderTabs() {
    automated(id = "1.11-15", title = "Tabs are reordered by dragging them along the row") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        expandInTree(name = "Recipes")
        clickInTree(text = WELCOME)
        clickInTree(text = "Arepas")
        clickInTree(text = "Ideas")
        expectTabOrder(names = listOf(WELCOME, "Arepas", "Ideas"))
        dragTab(
            name = WELCOME,
            by = Offset(x = tabLeft(name = "Ideas") - DROP_MARGIN - tabCenterX(name = WELCOME), y = 0f)
        )
        expectTabOrder(names = listOf("Arepas", WELCOME, "Ideas"))
        dragTab(
            name = "Ideas",
            by = Offset(x = tabLeft(name = WELCOME) - DROP_MARGIN - tabCenterX(name = "Ideas"), y = 0f)
        )
        expectTabOrder(names = listOf("Arepas", "Ideas", WELCOME))
        // Dropping a tab in the first place does not register with synthetic mouse input, though it does with a
        // person's mouse (the 1.11 smoke): 1.11-16 walks the tester through it.
    }
}

private fun SuiteBuilder.tabSlides() {
    manual(id = "1.11-16", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "A dragged tab slides along the row")
        steps(
            text = "$OPEN_PLAYGROUND\n2. Open three notes, then drag the last tab slowly along the row and let it go " +
                "in the first place."
        )
        expected(
            text = "While dragged, the tab stays on the row's line and the other tabs slide aside to open its place; " +
                "let go, it glides into the first place and stays there."
        )
    }
}

private fun CaseScope.openTwoNotes() {
    openVault(name = PLAYGROUND, shownText = WELCOME)
    expandInTree(name = "Recipes")
    clickInTree(text = WELCOME)
    clickInTree(text = "Arepas")
}

/** The card of a folder's grid showing [name]: of the elements showing it, the one furthest right. */
private fun CaseScope.card(name: String): SemanticsNodeInteraction {
    expectText(text = name)
    val nodes = ui.onAllNodes(matcher = hasText(text = name))
    val count = nodes.fetchSemanticsNodes().size
    return (0 until count).map {
        nodes[it]
    }.filter { it.isShown() }.maxBy { it.fetchSemanticsNode().boundsInRoot.left }
}

private fun SemanticsNodeInteraction.top(): Float = fetchSemanticsNode().boundsInRoot.top

private fun CaseScope.expectOnScreen(text: String) {
    ui.waitUntil(timeoutMillis = 10_000) {
        val nodes = ui.onAllNodes(matcher = hasText(text = text, substring = true))
        val count = nodes.fetchSemanticsNodes().size
        (0 until count).any { nodes[it].isShown() }
    }
}

private fun CaseScope.clickInOutline(heading: String) {
    val row = hasText(text = heading) and hasAnyAncestor(
        matcher = hasContentDescription(value = "Headings of this note")
    )
    ui.waitUntil(timeoutMillis = 10_000) { ui.onAllNodes(matcher = row).fetchSemanticsNodes().isNotEmpty() }
    ui.onAllNodes(matcher = row)[0].performClick()
}

private const val DROP_MARGIN = 8f
private const val QUICK_CLICKS = 20

package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import app.notekea.selftest.openVault
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.relaunch
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder
import com.testbuddy.WindowSize

private const val OUTLINE_NOTE = "Outline practice"
private const val OUTLINE_FILE = "$PLAYGROUND/Editor/Outline practice.md"
private const val OUTLINE_SHOWN = "Arepas guide"
private const val OUTLINE_LABEL = "Headings of this note"
private const val NARROW_WIDTH = 900
private const val NARROW_HEIGHT = 800

/** The cases of 1.9, the outline of the side panel (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.outlineCases() {
    outlineLists()
    outlineClick()
    outlineHighlight()
    outlineInPreview()
    sidePanelHides()
    narrowSidePanel()
    outlineOfNewNote()
}

private fun SuiteBuilder.outlineLists() {
    automated(id = "1.9-01", title = "The outline lists the headings, nested, and only headings") {
        openPlaygroundNote(name = OUTLINE_NOTE, shownText = OUTLINE_SHOWN)
        listOf("Ingredients", "For the filling and the shopping list", "Steps", "Tip: resting the dough", "Cooking")
            .forEach { expectText(text = it) }
        listOf("Not headings", "Serving", "Last section").forEach { expectText(text = it) }
        expectNoText(text = "#recipe")
        expectNoText(text = "a comment inside a code block")
        val isNested = left("Ingredients") < left("For the filling and the shopping list") &&
            left("Tip: resting the dough") == left("Cooking") && left("Serving") < left("Steps")
        if (!isNested) throw CaseFailure(message = "expected the headings indented by their level")
    }
}

private fun SuiteBuilder.outlineClick() {
    automated(id = "1.9-02", title = "Clicking a heading goes there, ready to type") {
        openPlaygroundNote(name = OUTLINE_NOTE, shownText = OUTLINE_SHOWN)
        clickOutline(heading = "Steps")
        type(text = " today")
        expectFile(path = OUTLINE_FILE, containing = "## Steps today")
        expectText(text = "Steps today")
        clickOutline(heading = "Cooking")
        expectSelected(heading = "Cooking")
    }
}

private fun SuiteBuilder.outlineHighlight() {
    automated(id = "1.9-03", title = "The last heading stays highlighted after a click") {
        openPlaygroundNote(name = OUTLINE_NOTE, shownText = OUTLINE_SHOWN)
        clickOutline(heading = "Last section")
        expectSelected(heading = "Last section")
    }
}

private fun SuiteBuilder.outlineInPreview() {
    automated(id = "1.9-04", title = "The outline works in Preview too") {
        openPlaygroundNote(name = OUTLINE_NOTE, shownText = OUTLINE_SHOWN)
        pressInWindow(shortcut = Mod + Key.E)
        clickOutline(heading = "Serving")
        expectSelected(heading = "Serving")
    }
}

private fun SuiteBuilder.sidePanelHides() {
    automated(id = "1.9-06", title = "The side panel hides and shows, and stays as it was left") {
        openPlaygroundNote(name = OUTLINE_NOTE, shownText = OUTLINE_SHOWN)
        clickIcon(label = "Minimize side panel")
        expectNoText(text = "Ingredients")
        clickIcon(label = "Side panel")
        expectText(text = "Ingredients")
        press(shortcut = Mod + Shift + Key.Backslash)
        expectNoText(text = "Ingredients")
        relaunch()
        expectNoText(text = "Ingredients")
        pressInWindow(shortcut = Mod + Shift + Key.O)
        expectText(text = "Ingredients")
    }
}

private fun SuiteBuilder.narrowSidePanel() {
    automated(
        id = "1.9-07",
        title = "In a narrow window the side panel floats over the note",
        window = WindowSize(width = NARROW_WIDTH, height = NARROW_HEIGHT)
    ) {
        openPlaygroundNote(name = OUTLINE_NOTE, shownText = OUTLINE_SHOWN)
        expectNoText(text = "Ingredients")
        clickIcon(label = "Side panel")
        clickOutline(heading = "Cooking")
        expectNoText(text = "Ingredients")
        pressInWindow(shortcut = Mod + Shift + Key.O)
        expectText(text = "Ingredients")
        press(key = Key.Escape)
        expectNoText(text = "Ingredients")
    }
}

private fun SuiteBuilder.outlineOfNewNote() {
    automated(id = "1.9-08", title = "A new note's outline fills in as headings are typed") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        pressInWindow(shortcut = Mod + Key.N)
        press(key = Key.Enter)
        expectText(text = "This note has no headings")
        typeInNoteBody(text = "# Hello")
        expectText(text = "Hello")
    }
}

private fun CaseScope.typeInNoteBody(text: String) {
    val body = hasSetTextAction() and hasContentDescription(value = "Note text")
    ui.waitUntil(timeoutMillis = 10_000) { ui.onAllNodes(matcher = body).fetchSemanticsNodes().isNotEmpty() }
    ui.onAllNodes(matcher = body).onFirst().apply {
        performClick()
        performTextInput(text = text)
    }
}

// A heading's row in the outline: the note itself shows the same words in Preview.
private fun inOutline(heading: String) =
    hasText(text = heading) and hasAnyAncestor(matcher = hasContentDescription(value = OUTLINE_LABEL))

private fun CaseScope.clickOutline(heading: String) {
    ui.waitUntil(timeoutMillis = 10_000) {
        ui.onAllNodes(matcher = inOutline(heading)).fetchSemanticsNodes().isNotEmpty()
    }
    ui.onAllNodes(matcher = inOutline(heading)).onFirst().performClick()
}

private fun CaseScope.expectSelected(heading: String) {
    ui.waitUntil(timeoutMillis = 10_000) {
        ui.onAllNodes(matcher = isSelected() and hasAnyDescendant(matcher = hasText(text = heading)))
            .fetchSemanticsNodes().isNotEmpty()
    }
}

// The label itself, not its row: rows are as wide as the outline, labels start further right the deeper they are.
private fun CaseScope.left(text: String): Float = ui.onAllNodes(
    matcher = hasText(text = text),
    useUnmergedTree = true
).onFirst().fetchSemanticsNode().boundsInRoot.left

package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import app.notekea.selftest.clickInTree
import app.notekea.selftest.noteText
import app.notekea.selftest.openNote
import app.notekea.selftest.openVault
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.relaunch
import app.notekea.selftest.rightClickInTree
import app.notekea.selftest.treeRow
import app.notekea.selftest.waitForResult
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder
import com.testbuddy.WindowSize
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private const val TEMPLATES_VAULT = "Templates Vault"
private const val KICKOFF = "Kickoff notes"
private const val KICKOFF_FILE = "$TEMPLATES_VAULT/Projects/Kickoff notes.md"
private const val KICKOFF_TEXT = "Goals for the launch."
private const val WELCOME_TEXT = "This is a sample vault"
private const val PANEL_WIDTH_SLACK = 20f
private const val WIDE_WIDTH = 1700
private const val WIDE_HEIGHT = 900

/** The cases of 1.12, templates, and the panels' first width (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.templateCases() {
    firstTemplate()
    templatesFromSettings()
    emptyNoteBecomesTemplate()
    newNoteFromTemplate()
    templateForFolder()
    templateReplacesSelection()
    templatesInObsidian()
    panelsFirstWidth()
}

private fun SuiteBuilder.firstTemplate() {
    automated(id = "1.12-01", title = "Without templates, Create makes an example and inserts it") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = WELCOME)
        cursorAtEnd(part = WELCOME_TEXT)
        press(shortcut = Mod + Key.T)
        expectText(text = "No templates folder yet")
        expectText(text = "Create a Templates folder to add templates. It starts with an example you can change.")
        click(text = "Create")
        expectFile(path = "$PLAYGROUND/Templates/Meeting notes.md")
        waitForResult(name = "Meeting notes")
        click(text = "Meeting notes")
        val welcome = "$PLAYGROUND/Welcome.md"
        expectFile(path = welcome, containing = "Date: ${LocalDate.now()} at ")
        listOf("## Attendees", "## Action items", "tags: meeting").forEach {
            expectFile(path = welcome, containing = it)
        }
    }
}

private fun SuiteBuilder.templatesFromSettings() {
    automated(id = "1.12-02", title = "Obsidian's template settings decide the folder and the formats") {
        openVault(name = TEMPLATES_VAULT, shownText = "Projects")
        openNote(name = KICKOFF, shownText = KICKOFF_TEXT)
        cursorAtEnd(part = KICKOFF_TEXT)
        press(shortcut = Mod + Key.T)
        expectText(text = "Find a template to insert")
        waitForResult(name = "Meeting")
        waitForResult(name = "Work/Weekly review")
        type(text = "wee")
        expectNoText(text = "Meeting")
        clearText()
        waitForResult(name = "Meeting")
        press(key = Key.Enter)
        val today = LocalDate.now()
        val date = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " (" +
            today.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)) + ")"
        expectFile(path = KICKOFF_FILE, containing = "Date: $date at ")
        val text = Files.readString(file(path = KICKOFF_FILE))
        val isTwelveHour = Regex(pattern = """at \d{1,2}:\d{2} (AM|PM)""").containsMatchIn(text)
        val hasProperties = listOf("project", "meeting", "status: active", "type: meeting").all {
            it in
                text.substringBefore("# ")
        }
        if (!isTwelveHour ||
            !hasProperties
        ) {
            throw CaseFailure(message = "expected h:mm AM or PM and the merged properties")
        }
    }
}

private fun SuiteBuilder.emptyNoteBecomesTemplate() {
    // A wide window, so the toolbar has room for Insert template outside More.
    automated(
        id = "1.12-03",
        title = "Insert template in the toolbar fills an empty note with the template",
        window = WindowSize(width = WIDE_WIDTH, height = WIDE_HEIGHT)
    ) {
        openVault(name = TEMPLATES_VAULT, shownText = "Projects")
        openNote(name = "Empty note", shownText = "Empty note")
        clickIcon(label = "Insert template")
        waitForResult(name = "Meeting")
        press(key = Key.Enter)
        val note = "$TEMPLATES_VAULT/Projects/Empty note.md"
        listOf("meeting", "type: meeting", "# Empty note", "## Attendees").forEach {
            expectFile(path = note, containing = it)
        }
    }
}

private fun SuiteBuilder.newNoteFromTemplate() {
    automated(id = "1.12-04", title = "New note from template makes a named note from it") {
        openVault(name = TEMPLATES_VAULT, shownText = "Projects")
        rightClickInTree(text = "Projects")
        click(text = "New note from template")
        expectText(text = "Choose a template for the new note")
        waitForResult(name = "Meeting")
        press(key = Key.Enter)
        expectFile(path = "$TEMPLATES_VAULT/Projects/Untitled.md")
        waitForNameField(name = "Untitled")
        clearText()
        type(text = "Team sync")
        press(key = Key.Enter)
        val note = "$TEMPLATES_VAULT/Projects/Team sync.md"
        expectFile(path = note, containing = "# Team sync")
        expectFile(path = note, containing = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
    }
}

private fun SuiteBuilder.templateForFolder() {
    automated(id = "1.12-05", title = "Mod+T on a folder makes a new note there from a template") {
        openVault(name = TEMPLATES_VAULT, shownText = "Projects")
        clickInTree(text = "Projects")
        pressInWindow(shortcut = Mod + Key.T)
        type(text = "weekly")
        waitForResult(name = "Work/Weekly review")
        press(key = Key.Enter)
        waitForNameField(name = "Untitled")
        press(key = Key.Escape)
        val week = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH))
        expectFile(path = "$TEMPLATES_VAULT/Projects/Untitled.md", containing = "## Week of $week")
        expectFile(path = "$TEMPLATES_VAULT/Projects/Untitled.md", containing = "- Next:")
    }
}

private fun SuiteBuilder.templateReplacesSelection() {
    automated(id = "1.12-06", title = "A template replaces the selection, and one undo takes it back") {
        openVault(name = TEMPLATES_VAULT, shownText = "Projects")
        openNote(name = KICKOFF, shownText = KICKOFF_TEXT)
        pressInWindow(shortcut = Mod + Key.E)
        pressInWindow(shortcut = Mod + Key.T)
        expectText(text = "Find a template to insert")
        press(key = Key.Escape)
        val field = noteText(part = KICKOFF_TEXT)
        val start = field.fetchSemanticsNode().config[SemanticsProperties.EditableText].text.indexOf("Goals")
        field.performTextInputSelection(selection = TextRange(start = start, end = start + "Goals".length))
        press(shortcut = Mod + Key.T)
        type(text = "weekly")
        waitForResult(name = "Work/Weekly review")
        press(key = Key.Enter)
        expectFile(path = KICKOFF_FILE, containing = "- Next:")
        expectNoText(text = "Goals for", substring = true)
        press(shortcut = Mod + Key.Z)
        expectFile(path = KICKOFF_FILE, containing = KICKOFF_TEXT)
    }
}

private fun SuiteBuilder.templatesInObsidian() {
    manual(id = "1.12-07", reason = ManualReason.SYSTEM) {
        prepare(TEMPLATES_VAULT)
        title(text = "Obsidian lists the same templates")
        steps(
            text = "If Obsidian is not installed, press Skip for now.\n1. Open the Templates Vault from the Test " +
                "data folder in Obsidian and turn on the Templates core plugin.\n2. Open Projects › Kickoff notes " +
                "and run \"Templates: Insert template\"."
        )
        expected(
            text = "Obsidian lists the same templates of Snippets and fills in the date and time in the same formats " +
                "as Notekea."
        )
    }
}

private fun SuiteBuilder.panelsFirstWidth() {
    automated(id = "1.12-08", title = "On a first launch the sidebar and the side panel are as wide") {
        openPlaygroundNote(name = "Outline practice", shownText = "Arepas guide")
        expectSameWidths()
        relaunch()
        expectSameWidths()
    }
}

/** Waits until the tree's name field of a new entry, showing [name], has the keyboard. */
private fun CaseScope.waitForNameField(name: String) {
    ui.waitUntil(timeoutMillis = 10_000) {
        ui.onAllNodes(
            matcher = isFocused() and hasSetTextAction() and hasText(text = name)
        ).fetchSemanticsNodes().isNotEmpty()
    }
}

// Both panels open 340 dp wide on a first launch: a row of the tree and a row of the outline span their panel.
private fun CaseScope.expectSameWidths() {
    expectText(text = "Ingredients")
    val tree = treeRow(text = WELCOME).fetchSemanticsNode().boundsInRoot.width
    val outline = treeRowOf(text = "Ingredients")
    if (abs(tree - outline) >
        PANEL_WIDTH_SLACK
    ) {
        throw CaseFailure(message = "expected the panels as wide, $tree and $outline")
    }
}

private fun CaseScope.treeRowOf(text: String): Float {
    val nodes = ui.onAllNodes(matcher = androidx.compose.ui.test.hasText(text = text))
    val count = nodes.fetchSemanticsNodes().size
    return (0 until count).maxOf { nodes[it].fetchSemanticsNode().boundsInRoot.left }.let { left ->
        (0 until count).map { nodes[it].fetchSemanticsNode().boundsInRoot }.first { it.left == left }.width
    }
}

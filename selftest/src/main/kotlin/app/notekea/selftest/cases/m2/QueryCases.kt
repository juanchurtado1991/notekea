package app.notekea.selftest.cases.m2

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasText
import app.notekea.selftest.clickLast
import app.notekea.selftest.expectOrderOnScreen
import app.notekea.selftest.openNote
import app.notekea.selftest.openVault
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.relaunch
import app.notekea.selftest.typeAtEnd
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val HOME = "Home"
private const val QUERY = "Query"
private const val STATE_BLOCKS = "\n\n```query\nfilters:\n  - property: rating\n    condition: more than\n" +
    "    value: 99\n```\n\n```query\nsorted: rating\n```\n\n```query\ncolumns: [mood]\n```\n"
private const val OPEN_QUERY =
    "1. In Notekea, open the Sample Vault (see TEST DATA above).\n2. Click Query in the rail on the left."

/** The cases of 2.10, vault queries (`MVP_SMOKE_TEST.md`): the Query tab and the query blocks of notes. */
fun SuiteBuilder.queryCases() {
    queryTab()
    booksByRating()
    projectsAndMeetings()
    cellsEditNotes()
    savedInANote()
    queryStates()
    queryInObsidian()
}

private fun SuiteBuilder.queryTab() {
    automated(id = "2.10-01", title = "The Query tab lists the vault's notes") {
        openVault(name = SAMPLE, shownText = "Home")
        clickIcon(label = "Query")
        expectText(text = "Add filter")
        expectText(text = "40 of 40 notes")
    }
}

private fun SuiteBuilder.booksByRating() {
    automated(id = "2.10-02", title = "Books by rating list the right notes, in order, and the query stays") {
        openVault(name = SAMPLE, shownText = HOME)
        clickIcon(label = QUERY)
        addFolderRule(folder = "Books")
        addRule(property = "rating", condition = "more than", value = "4")
        expectText(text = "Deep Work")
        expectText(text = "Dune")
        expectNoText(text = "Sapiens")
        click(text = "4")
        clearText()
        type(text = "3")
        press(key = Key.Enter)
        expectText(text = "Sapiens")
        sortBy(property = "rating")
        expectOrderOnScreen(texts = listOf("Sapiens", "Dune"))
        click(text = "Sort")
        scrollTo(text = "High first")
        clickLast(text = "High first")
        expectOrderOnScreen(texts = listOf("Dune", "Sapiens"))
        relaunch()
        expectText(text = HOME)
        clickIcon(label = QUERY)
        expectOrderOnScreen(texts = listOf("Dune", "Sapiens"))
    }
}

private fun SuiteBuilder.projectsAndMeetings() {
    automated(id = "2.10-03", title = "Projects by status and meetings by date list the right notes") {
        openVault(name = SAMPLE, shownText = HOME)
        clickIcon(label = QUERY)
        addFolderRule(folder = "Projects")
        sortBy(property = "status")
        expectOrderOnScreen(texts = listOf("Notekea launch", "Kitchen renovation", "Garden plan", "Trip to Lisbon"))
        clickIcon(label = "Remove filter")
        addFolderRule(folder = "Meetings")
        addRule(property = "date", condition = "after", value = "2026-10-22")
        sortBy(property = "date")
        expectOrderOnScreen(texts = listOf("2026-10-23 Design review", "2026-10-24 Budget", "Retro October"))
        expectNoText(text = "Vendor call")
        addRule(property = "rating", condition = "more than", value = "high")
        expectText(text = "Retro October")
        expectNoText(text = "No notes match these filters")
    }
}

private fun SuiteBuilder.cellsEditNotes() {
    manual(id = "2.10-04", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "A cell edits its note, and an edit in the note reaches the table")
        steps(
            text = "$OPEN_QUERY\n3. Add the filter Folder, Books, and the columns rating and finished.\n4. Change a " +
                "book's rating in its cell, press Enter, and click its finished box.\n5. Click the book's name, and " +
                "look at its properties.\n6. Change its rating there, and go back to the Query tab."
        )
        expected(
            text = "The table shows each change at once. The note holds the new rating and finished, the rest of " +
                "its frontmatter unchanged. Within a second of step 6 the table shows the note's new rating."
        )
    }
}

private fun SuiteBuilder.savedInANote() {
    automated(id = "2.10-05", title = "A query saved in a note is a table in Preview and YAML in the note") {
        openVault(name = SAMPLE, shownText = HOME)
        openNote(name = "Reading list", shownText = "Deep Work")
        clickIcon(label = QUERY)
        addFolderRule(folder = "Books")
        click(text = "Save in Reading list")
        expectText(text = "Saved in Reading list.")
        click(text = "Save in Reading list")
        expectText(text = "Reading list already holds this query.")
        expectFile(path = "$SAMPLE/Books/Reading list.md", containing = "    value: Books\n```")
        click(text = "Reading list")
        pressInWindow(shortcut = Mod + Key.E)
        expectText(text = "Folder is Books")
    }
}

private fun SuiteBuilder.queryStates() {
    automated(id = "2.10-06", title = "A broken block, an unknown property and no match each say so") {
        openVault(name = SAMPLE, shownText = "Home")
        openNote(name = "Reading list", shownText = "Deep Work")
        typeAtEnd(part = "Deep Work", text = STATE_BLOCKS)
        pressInWindow(shortcut = Mod + Key.E)
        expectText(text = "This query can't be read")
        expectText(text = "Line 1: a query holds only filters, sort and columns.")
        expectText(text = "0 notes")
        expectText(text = "No note has the property mood.")
    }
}

private fun SuiteBuilder.queryInObsidian() {
    manual(id = "2.10-07", reason = ManualReason.SYSTEM) {
        prepare(SAMPLE)
        title(text = "A note with a query block opens unchanged in Obsidian")
        steps(
            text = "1. Save a query in Books/Reading list, as in 2.10-05.\n2. Open that vault in Obsidian and open " +
                "the note, in Reading view and in Source mode."
        )
        expected(text = "Obsidian shows the block as a code block, and the rest of the note as before.")
    }
}

/** Adds the rule Folder is [folder], from the Add filter button or the + after the last rule. */
private fun CaseScope.addFolderRule(folder: String) {
    val hasButton = ui.onAllNodes(matcher = hasText(text = "Add filter")).fetchSemanticsNodes().isNotEmpty()
    if (hasButton) click(text = "Add filter") else clickIcon(label = "Add filter")
    clickLast(text = "Choose…")
    clickLast(text = folder)
}

/** Adds the rule [property] [condition] [value] after the last one, typing the value. */
private fun CaseScope.addRule(
    property: String,
    condition: String,
    value: String
) {
    clickIcon(label = "Add filter")
    clickLast(text = "Folder")
    clickLast(text = property)
    clickLast(text = "is")
    clickLast(text = condition)
    clickLast(text = "Value")
    type(text = value)
    press(key = Key.Enter)
}

/** Sorts the table by [property], low first. */
private fun CaseScope.sortBy(property: String) {
    click(text = "Sort")
    clickLast(text = property)
    press(key = Key.Escape)
}

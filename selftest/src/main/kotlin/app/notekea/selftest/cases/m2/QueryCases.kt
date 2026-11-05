package app.notekea.selftest.cases.m2

import app.notekea.selftest.openVault
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
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
    manual(id = "2.10-02", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "Books by rating list the right notes, in order, and the query stays")
        steps(
            text = "$OPEN_QUERY\n3. Click Add filter, keep Folder, and choose Books as its value.\n4. Click the + of " +
                "that rule, choose rating, then more than, type 4 and press Enter.\n5. Click Columns and choose " +
                "author and rating, then close the menu.\n6. In the rating rule, change 4 to 3 and press Enter." +
                "\n7. Click the " +
                "rating column's name twice." +
                "\n8. Close and open the vault, and click Query."
        )
        expected(
            text = "Two rules show the filters. Step 4 lists Deep Work, Dune and The Pragmatic Programmer, and step " +
                "6 adds Atomic Habits and Sapiens. The first click sorts low first and the second high first, with " +
                "an arrow on the column. After reopening, the same query and table are there."
        )
    }
}

private fun SuiteBuilder.projectsAndMeetings() {
    manual(id = "2.10-03", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "Projects by status and meetings by date list the right notes")
        steps(
            text = "$OPEN_QUERY\n3. Add the rule Folder, Projects, and click Sort, status.\n4. Remove the rule with " +
                "its -, add the rule Folder, Meetings, and the rule date, after, 2026-10-22, and click Sort, date." +
                "\n5. Add the rule rating, more than, type high and press Enter."
        )
        expected(
            text = "Step 3 starts with Notekea launch (active), Kitchen renovation (on hold) and Garden plan " +
                "(planning), and the projects with no status come last. Step 4 lists Design review, Budget and " +
                "Retro October, oldest first. In step 5 the field is outlined in red and " +
                "the table does not change."
        )
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
    manual(id = "2.10-05", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "A query saved in a note is a live table in Preview and YAML in Edit")
        steps(
            text =
            "1. In Notekea, open the Sample Vault (see TEST DATA above) and open Books/Reading list.\n2. Click " +
                "Query in the rail, and add the filter Folder, Books, and the column rating.\n3. Click Save in " +
                "Reading list.\n4. Go to the note's tab, in Preview.\n5. Change a rating in the table.\n6. Click " +
                "Edit, then close and open the note."
        )
        expected(
            text = "The tab says Saved in Reading list. In Preview the block is a table under the query's chips, " +
                "with a border; its header does not sort. A second click on Save says the note already holds it. " +
                "Step 5 changes that book. In Edit the block is the " +
                "query's YAML between ```query and ```, at the end of the note."
        )
    }
}

private fun SuiteBuilder.queryStates() {
    manual(id = "2.10-06", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "No match, an unknown property and a broken block each say so")
        steps(
            text = "1. In Notekea, open the Sample Vault (see TEST DATA above) and open Books/Reading list, in " +
                "Edit.\n2. At its end, write a block with three backticks and query, the line sorted: rating, and " +
                "three backticks.\n3. Click Preview.\n4. In Edit, replace that line with columns: [mood], then " +
                "Preview.\n5. Click Query in the rail, and add a filter that no note passes, such as rating, more " +
                "than, 99."
        )
        expected(
            text = "Step 3 says This query can't be read, names the line and shows the YAML below. Step 4 lists " +
                "every note and says No note has the property mood. Step 5 says No notes match these filters."
        )
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

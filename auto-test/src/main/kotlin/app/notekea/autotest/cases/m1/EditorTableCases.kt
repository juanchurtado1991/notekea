package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val LISBON = "Lisbon trip"
private const val LISBON_TEXT = "Before leaving"
private const val LAST_ROW = "| 3 | Day trip to Sintra and Pena a\\|b |"

/** The cases of a table edited by cells, part of 1.5 since One editing mode (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.editorTableCases() {
    cellsEdited()
    tableBarLook()
}

private fun SuiteBuilder.cellsEdited() {
    automated(
        id = "1.5-14",
        title = "A table is edited by cells: typing, Tab, and the bar's rows and columns"
    ) {
        openPlaygroundNote(
            name = LISBON,
            shownText = LISBON_TEXT
        )

        cursorAt(
            words = "Day trip to Sintra",
            part = LISBON_TEXT
        )

        type(text = " and Pena a|b")

        expectInNote(
            text = LAST_ROW,
            part = LISBON_TEXT
        )

        press(key = Key.Tab)

        expectInNote(
            text = "$LAST_ROW\n|  |  |",
            part = LISBON_TEXT
        )

        type(text = "4")
        press(key = Key.Tab)
        type(text = "Beach")

        expectInNote(
            text = "| 4 | Beach |",
            part = LISBON_TEXT
        )

        clickIcon(label = "Insert column right")

        expectInNote(
            text = "| Day | Plan |  |",
            part = LISBON_TEXT
        )

        clickIcon(label = "Delete row")

        expectInNote(
            text = "$LAST_ROW  |\n\n",
            part = LISBON_TEXT
        )
    }
}

private fun SuiteBuilder.tableBarLook() {
    manual(
        id = "1.5-15",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "A table's cell, its bar and its menu")

        steps(
            text =
            "1. In Notekea, open the Playground Vault (see TEST DATA above).\n2. Open the note Travel › Lisbon " +
                "trip and click the cell \"Alfama walk, tram 28\".\n3. Press Tab, Shift+Tab, Enter and the " +
                "arrows.\n4. In the bar over the table click Align column center, then Align column right.\n" +
                "5. Right-click another cell.\n6. Press the Down arrow on the last row.\n7. Click Source (</>) " +
                "at the end of the toolbar."
        )

        expected(
            text =
            "The clicked cell gets a frame in the accent color with the cursor in it, and a small bar shows " +
                "over the table: insert row above and below, insert column left and right, align left, center and " +
                "right, delete row, delete column, each with a tooltip. Tab and Shift+Tab go to the next and the " +
                "previous cell, Enter to the cell below, the arrows to the neighbour cells. The column is drawn " +
                "centered, then right-aligned. The right click moves the cursor to that cell and offers the " +
                "bar's actions. Down on the last row leaves the table and the bar goes. Source shows the table " +
                "as written, its second row saying the alignment (--:)."
        )
    }
}

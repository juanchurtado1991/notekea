package app.notekea.autotest.cases.m2

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasText
import app.notekea.autotest.clickLast
import app.notekea.autotest.cursorToNoteStart
import app.notekea.autotest.expectOrderOnScreen
import app.notekea.autotest.openNote
import app.notekea.autotest.openVault
import app.notekea.autotest.relaunch
import app.notekea.autotest.typeAtEnd
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val HOME = "Home"
private const val QUERY = "Query"
private const val STATE_BLOCKS =
    "\n\n```query\nfilters:\n  - property: rating\n    condition: more than\n" +
        "    value: 99\n```\n\n```query\nsorted: rating\n```\n\n```query\ncolumns: [mood]\n```\n"
private const val OPEN_QUERY =
    "1. En Notekea, abre la Sample Vault (mira DATOS DE PRUEBA arriba).\n2. Haz clic en Consulta en la barra de la izquierda."

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
    automated(
        id = "2.10-01",
        title = "The Query tab lists the vault's notes"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Home"
        )

        clickIcon(label = "Query")
        expectText(text = "Add filter")
        expectText(text = "40 of 40 notes")
    }
}

private fun SuiteBuilder.booksByRating() {
    automated(
        id = "2.10-02",
        title = "Books by rating list the right notes, in order, and the query stays"
    ) {
        openVault(
            name = SAMPLE,
            shownText = HOME
        )

        clickIcon(label = QUERY)
        addFolderRule(folder = "Books")

        addRule(
            property = "rating",
            condition = "more than",
            value = "4"
        )

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
    automated(
        id = "2.10-03",
        title = "Projects by status and meetings by date list the right notes"
    ) {
        openVault(
            name = SAMPLE,
            shownText = HOME
        )

        clickIcon(label = QUERY)
        addFolderRule(folder = "Projects")
        sortBy(property = "status")

        expectOrderOnScreen(
            texts = listOf("Notekea launch", "Kitchen renovation", "Garden plan", "Trip to Lisbon")
        )

        clickIcon(label = "Remove filter")
        addFolderRule(folder = "Meetings")

        addRule(
            property = "date",
            condition = "after",
            value = "2026-10-22"
        )

        sortBy(property = "date")

        expectOrderOnScreen(
            texts = listOf("2026-10-23 Design review", "2026-10-24 Budget", "Retro October")
        )

        expectNoText(text = "Vendor call")

        addRule(
            property = "rating",
            condition = "more than",
            value = "high"
        )

        expectText(text = "Retro October")
        expectNoText(text = "No notes match these filters")
    }
}

private fun SuiteBuilder.cellsEditNotes() {
    manual(
        id = "2.10-04",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "Una celda edita su nota, y una edición en la nota llega a la tabla")

        steps(
            text =
            "$OPEN_QUERY\n3. Agrega el filtro Carpeta, Books, y las columnas rating y " +
                "finished.\n4. Cambia el rating de un libro en su celda, pulsa Enter y haz clic " +
                "en su casilla finished.\n5. Haz clic en el nombre del libro y mira sus " +
                "propiedades.\n6. Cambia ahí su rating y vuelve a la pestaña Consulta."
        )

        expected(
            text =
            "La tabla muestra cada cambio al instante. La nota tiene el nuevo rating y " +
                "finished, y el resto de su frontmatter sin cambios. En menos de un segundo " +
                "después del paso 6 la tabla muestra el nuevo rating de la nota."
        )
    }
}

private fun SuiteBuilder.savedInANote() {
    automated(
        id = "2.10-05",
        title = "A query saved in a note is a table in the note and YAML in its file"
    ) {
        openVault(
            name = SAMPLE,
            shownText = HOME
        )

        openNote(
            name = "Reading list",
            shownText = "Deep Work"
        )

        clickIcon(label = QUERY)
        addFolderRule(folder = "Books")
        click(text = "Save in Reading list")
        expectText(text = "Saved in Reading list.")
        click(text = "Save in Reading list")
        expectText(text = "Reading list already holds this query.")

        expectFile(
            path = "$SAMPLE/Books/Reading list.md",
            containing = "    value: Books\n```"
        )

        click(text = "Reading list")
        cursorToNoteStart()
        expectText(text = "Folder is Books")
    }
}

private fun SuiteBuilder.queryStates() {
    automated(
        id = "2.10-06",
        title = "A broken block, an unknown property and no match each say so"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Home"
        )

        openNote(
            name = "Reading list",
            shownText = "Deep Work"
        )

        typeAtEnd(
            part = "Deep Work",
            text = STATE_BLOCKS
        )

        cursorToNoteStart()
        expectText(text = "This query can't be read")
        expectText(text = "Line 1: a query holds only filters, sort and columns.")
        expectText(text = "0 notes")
        // The note is a lazy list: the last block is made once the page is scrolled to it.
        scrollTo(text = "No note has the property mood.")
        expectText(text = "No note has the property mood.")
    }
}

private fun SuiteBuilder.queryInObsidian() {
    manual(
        id = "2.10-07",
        reason = ManualReason.SYSTEM
    ) {
        prepare(SAMPLE)
        title(text = "Una nota con un bloque de consulta se abre sin cambios en Obsidian")

        steps(
            text =
            "1. Guarda una consulta en Books/Reading list, como en 2.10-05.\n2. Abre esa " +
                "bóveda en Obsidian y abre la nota, en la vista de lectura y en el modo fuente."
        )

        expected(
            text =
            "Obsidian muestra el bloque como un bloque de código, y el resto de la nota como " +
                "antes."
        )
    }
}

/** Adds the rule Folder is [folder], from the Add filter button or the + after the last rule. */
private fun CaseScope.addFolderRule(folder: String) {
    val hasButton = ui
        .onAllNodes(matcher = hasText(text = "Add filter"))
        .fetchSemanticsNodes()
        .isNotEmpty()

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

package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val DRAWN_TASK = "Knead the dough"
private const val LISBON = "Lisbon trip"
private const val LISBON_TEXT = "Before leaving"
private const val LAST_ROW = "| 3 | Day trip to Sintra and Pena a\\|b |"

/** The cases of a table edited by cells, part of 1.5 since One editing mode (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.editorTableCases() {
    cellsEdited()
    tableBarLook()
    drawnAgainUnder()
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
        title(text = "La celda de una tabla, su barra y su menú")

        steps(
            text =
            "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba).\n2. Abre " +
                "la nota Travel › Lisbon trip y haz clic en la celda \"Alfama walk, tram " +
                "28\".\n3. Pulsa Tab, Shift+Tab, Enter y las flechas.\n4. En la barra sobre la " +
                "tabla haz clic en Centrar la columna y luego en Alinear la columna a la " +
                "derecha.\n5. Haz clic derecho en otra celda.\n6. Pulsa la flecha abajo en la " +
                "última fila.\n7. Haz clic en Fuente (</>) al final de la barra de herramientas."
        )

        expected(
            text =
            "La celda en la que hiciste clic tiene un marco del color de acento con el " +
                "cursor dentro, y sobre la tabla aparece una barra pequeña: insertar fila arriba " +
                "y abajo, insertar columna a la izquierda y a la derecha, alinear a la " +
                "izquierda, al centro y a la derecha, eliminar fila, eliminar columna, cada una " +
                "con su descripción emergente. Tab y Shift+Tab van a la celda siguiente y a la " +
                "anterior, Enter a la celda de abajo, las flechas a las celdas vecinas. La " +
                "columna se dibuja centrada y luego alineada a la derecha. El clic derecho mueve " +
                "el cursor a esa celda y ofrece las acciones de la barra. Abajo en la última " +
                "fila sale de la tabla y la barra desaparece. Fuente muestra la tabla tal como " +
                "está escrita, con su segunda fila indicando la alineación (--:)."
        )
    }
}

private fun SuiteBuilder.drawnAgainUnder() {
    automated(
        id = "1.5-16",
        title = "A list is drawn again once the cursor is two lines under it"
    ) {
        openFormatting()
        cursorAtEnd()
        type(text = "\n\n- [ ] Knead the dough\n\n")

        // Drawn, the task is a text of its own; as written it is only part of the note's.
        expectText(text = DRAWN_TASK)
        type(text = "After the list")
        expectInNote(text = "- [ ] Knead the dough\n\nAfter the list")
    }
}

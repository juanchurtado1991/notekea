package app.notekea.autotest.cases.m2

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import app.notekea.autotest.openNote
import app.notekea.autotest.openVault
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val TAB_CLICKS = 3
private const val TAB_MILLIS = 2_000L
private const val AREPAS = "Arepas"
private const val AREPAS_TEXT = "pre-cooked corn flour"
private const val TASKS_LINE = "Buy flour for the arepas"
private const val OPEN_AREPAS =
    "1. En Notekea, abre la Sample Vault (mira DATOS DE PRUEBA arriba) y espera a que la barra de abajo " +
        "diga \"41 notas listas para buscar\".\n2. Abre Recipes/Arepas y mira Conexiones en el panel de la derecha."

/** The cases of 2.5, backlinks (`MVP_SMOKE_TEST.md`): Connections' Linked and Unlinked tabs. */
fun SuiteBuilder.backlinksCases() {
    linkedMentions()
    linkUnlinked()
    connectionsLook()
    rightClickMenus()
}

private fun SuiteBuilder.linkedMentions() {
    automated(
        id = "2.5-01",
        title = "Linked lists the notes that link to the open one, embeds and headings too"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Recipes"
        )

        openNote(
            name = AREPAS,
            shownText = AREPAS_TEXT
        )

        openConnections(tab = "Linked")
        // Weekly menu links to Arepas and embeds its ^dough block; Embeds embeds its Ingredients heading.
        expectText(text = "Recipes · 2 mentions")

        expectText(
            text = "Arepas#Ingredients",
            substring = true
        )

        expectText(
            text = "Best next to Arepas",
            substring = true
        )
    }
}

private fun SuiteBuilder.linkUnlinked() {
    automated(
        id = "2.5-02",
        title = "Link turns a plain mention into a link, and it moves to Linked"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Recipes"
        )

        openNote(
            name = AREPAS,
            shownText = AREPAS_TEXT
        )

        openConnections(tab = "Unlinked")

        expectText(
            text = TASKS_LINE,
            substring = true
        )

        click(text = "Link")

        expectFile(
            path = "$SAMPLE/Guides/Tasks.md",
            containing = "Buy flour for the [[Arepas|arepas]]"
        )

        openConnections(tab = "Linked")

        expectText(
            text = "Buy flour for the arepas",
            substring = true
        )
    }
}

/**
 * Clicks a [tab] of Connections until it is the selected one. The panel's sections still move while the note's
 * outline and counts come in, and a press that lands while they move takes the focus but selects nothing: macOS's
 * runner failed 2.5-01 and 2.5-02 that way, on Related with the focus on the tab clicked.
 */
private fun CaseScope.openConnections(tab: String) {
    val selected = hasText(text = tab) and isSelected()

    repeat(times = TAB_CLICKS) {
        click(text = tab)

        val isOpen = runCatching {
            ui.waitUntilAtLeastOneExists(
                matcher = selected,
                timeoutMillis = TAB_MILLIS
            )
        }.isSuccess

        if (isOpen) return
    }

    throw CaseFailure(message = "expected the $tab tab of Connections to open, it stayed closed")
}

private fun SuiteBuilder.rightClickMenus() {
    manual(
        id = "2.5-04",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)

        title(
            text =
            "Un clic derecho ofrece un menú en filas, encabezados, tarjetas y bóvedas " +
                "recientes"
        )

        steps(
            text =
            "$OPEN_AREPAS\n3. Haz clic en Enlazadas y haz clic derecho en una fila.\n4. Haz " +
                "clic derecho en un encabezado del Índice.\n5. Haz clic en la carpeta Recipes en " +
                "el árbol para abrir su pestaña, y haz clic derecho en una tarjeta.\n6. Cierra " +
                "la bóveda y haz clic derecho en una bóveda reciente."
        )

        expected(
            text =
            "La fila ofrece Abrir y Abrir al lado; el encabezado, Ir al encabezado; la " +
                "tarjeta, Agregar a marcadores, Renombrar, Mostrar en Archivos y Eliminar; y la " +
                "bóveda reciente, Abrir bóveda y Quitar."
        )
    }
}

private fun SuiteBuilder.connectionsLook() {
    manual(
        id = "2.5-03",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "Conexiones se lee bien, y una mención se abre donde está")

        steps(
            text =
            "$OPEN_AREPAS\n3. Haz clic por turnos en Relacionadas, Enlazadas y Sin " +
                "enlazar.\n4. En Enlazadas, haz clic en la fila Weekly menu; vuelve a Arepas y, " +
                "en otra fila, haz clic en Abrir al lado (aparece bajo el puntero).\n5. Cierra " +
                "la bóveda y ábrela de nuevo."
        )

        expected(
            text =
            "Cada pestaña dice cuántas contiene; cada fila muestra la nota, su carpeta y la " +
                "línea que nombra Arepas con el nombre resaltado, sin marcas de Markdown. Hacer " +
                "clic en Weekly menu la abre con la mención seleccionada y a la vista; Abrir al " +
                "lado divide el editor. Información de la nota se lee como una tabla pequeña. Al " +
                "reabrir, la pestaña que dejaste elegida sigue elegida."
        )
    }
}

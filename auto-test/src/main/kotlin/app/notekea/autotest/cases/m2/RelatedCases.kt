package app.notekea.autotest.cases.m2

import app.notekea.autotest.openNote
import app.notekea.autotest.openVault
import app.notekea.autotest.typeAtEnd
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val SAMPLE_READY = "41 notes ready to search"
private const val ALREADY_LINKED = "Already linked"
private const val WAIT_MILLIS = 10_000L
private const val OPEN_AREPAS =
    "1. En Notekea, abre la Sample Vault (mira DATOS DE PRUEBA arriba) y espera a que la barra de abajo " +
        "diga \"41 notas listas para buscar\".\n2. Abre Recipes/Arepas y mira Conexiones, en Relacionadas, en el panel de la " +
        "derecha."

/** The cases of 2.4, the related notes panel (`MVP_SMOKE_TEST.md`): the inspector's Related section. */
fun SuiteBuilder.relatedCases() {
    linkedLeftOut()
    newLinkLeftOut()
    byMeaning()
    rowActions()
}

private fun SuiteBuilder.linkedLeftOut() {
    automated(
        id = "2.4-01",
        title = "The notes a note links are under Already linked, not among its cards"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Recipes"
        )

        expectText(text = SAMPLE_READY)

        openNote(
            name = "Weekly menu",
            shownText = "Monday"
        )

        expectAlreadyLinked()
    }
}

private fun SuiteBuilder.newLinkLeftOut() {
    automated(
        id = "2.4-02",
        title = "A link typed in the note moves that note to Already linked"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Recipes"
        )

        expectText(text = SAMPLE_READY)

        openNote(
            name = "Arepas",
            shownText = "pre-cooked corn flour"
        )

        if (runCatching { scrollTo(text = ALREADY_LINKED) }.isSuccess) {
            throw CaseFailure(message = "expected no \"$ALREADY_LINKED\" before the note links one")
        }

        typeAtEnd(
            part = "pre-cooked corn flour",
            text = "\nSee [[Pancakes]]."
        )

        expectAlreadyLinked()
    }
}

/** The header sits below Related's rows, in a lazy list that makes it only when it is scrolled to. */
private fun CaseScope.expectAlreadyLinked() {
    ui
        .waitUntil(timeoutMillis = WAIT_MILLIS) {
            runCatching { scrollTo(text = ALREADY_LINKED) }.isSuccess
        }
}

private fun SuiteBuilder.byMeaning() {
    manual(
        id = "2.4-03",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)

        title(
            text =
            "Relacionadas muestra las notas más cercanas en significado, y sigue a la nota " +
                "abierta"
        )

        steps(
            text =
            "$OPEN_AREPAS\n3. Abre Recipes/Pancakes, luego Guides/Embeds, y mira " +
                "Relacionadas cada vez."
        )

        expected(
            text =
            "Cada vez, Relacionadas muestra primero notas del mismo tema (para Arepas, otras " +
                "recetas), cada fila con el nombre de la nota, su carpeta y un pasaje con la " +
                "frase más cercana resaltada. La nota abierta y las notas que enlaza no están " +
                "entre las filas; las notas que enlaza están bajo Ya enlazada. Cambia en menos " +
                "de un segundo al abrir otra nota."
        )
    }
}

private fun SuiteBuilder.rowActions() {
    manual(
        id = "2.4-04",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)

        title(
            text =
            "Una fila relacionada abre su nota, la abre al lado o inserta un enlace a ella"
        )

        steps(
            text =
            "$OPEN_AREPAS\n3. Pon el puntero sobre la primera fila y haz clic en su último " +
                "botón, Insertar enlace.\n4. Pon el puntero sobre otra fila y haz clic en su " +
                "primer botón, Abrir al lado.\n5. Haz clic en una fila."
        )

        expected(
            text =
            "Aparecen dos botones en la fila bajo el puntero (Abrir al lado, Insertar " +
                "enlace). Insertar enlace escribe un enlace a esa nota donde está el cursor en " +
                "Arepas, y la nota pasa a Ya enlazada. Abrir al lado divide el editor y abre la " +
                "nota en el otro panel. Hacer clic en una fila abre su nota."
        )
    }
}

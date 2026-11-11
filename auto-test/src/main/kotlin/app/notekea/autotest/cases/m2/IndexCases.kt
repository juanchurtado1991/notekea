package app.notekea.autotest.cases.m2

import app.notekea.autotest.openNote
import app.notekea.autotest.openVault
import app.notekea.autotest.typeAtEnd
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val GHOST = "Ghost Serializer"

// The 40 notes and Assets/Weekly menu.pdf, which 4.5 reads as a document.
private const val SAMPLE_READY = "41 notes ready to search"
private const val GHOST_READY = "371 notas listas para buscar"
private const val OPEN_GHOST =
    "1. En Notekea, abre la bóveda Ghost Serializer (mira DATOS DE PRUEBA arriba)."

/** The cases of 2.3, the hybrid index (`MVP_SMOKE_TEST.md`): the index follows the vault's notes. */
fun SuiteBuilder.indexCases() {
    vaultReady()
    editedNote()
    resumes()
    noticeAndDetails()
}

private fun SuiteBuilder.vaultReady() {
    automated(
        id = "2.3-01",
        title = "A vault's notes are indexed when it opens"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Recipes"
        )

        expectText(text = SAMPLE_READY)
    }
}

private fun SuiteBuilder.editedNote() {
    automated(
        id = "2.3-02",
        title = "An edited note is indexed again within seconds"
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

        typeAtEnd(
            part = "pre-cooked corn flour",
            text = "\nA pinch of salt."
        )

        expectText(text = "Saved")
        expectText(text = SAMPLE_READY)
        click(text = SAMPLE_READY)
        expectText(text = "Just now")
    }
}

private fun SuiteBuilder.resumes() {
    manual(
        id = "2.3-03",
        reason = ManualReason.PACKAGED
    ) {
        prepare(GHOST)
        title(text = "La indexación continúa después de cerrar Notekea a la mitad")

        steps(
            text =
            "$OPEN_GHOST\n2. Mientras la barra de abajo dice Indexando, haz clic en ella y " +
                "anota cuántas notas van (\"120 de 371 notas\").\n3. Cierra Notekea antes de que " +
                "termine, ábrelo de nuevo y vuelve a hacer clic en Indexando."
        )

        expected(
            text =
            "Sigue desde las notas que ya tenía: la cuenta y el porcentaje no vuelven a " +
                "empezar desde 0, y termina con \"$GHOST_READY\"."
        )
    }
}

private fun SuiteBuilder.noticeAndDetails() {
    manual(
        id = "2.3-04",
        reason = ManualReason.EYES
    ) {
        prepare(GHOST)
        title(text = "El aviso de la primera indexación y los detalles del índice")

        steps(
            text =
            "$OPEN_GHOST\n2. Mira la parte de arriba de la ventana y haz clic en Descartar " +
                "en el aviso que hay ahí.\n3. Haz clic en Indexando en la barra de abajo y mira " +
                "la tarjeta que se abre.\n4. Cuando diga \"$GHOST_READY\", haz clic en ella y " +
                "luego en Reconstruir el índice."
        )

        expected(
            text =
            "Un aviso dice que Notekea está leyendo tus notas y que puedes seguir " +
                "escribiendo; Descartar lo oculta. La tarjeta muestra una barra, cuántas notas " +
                "de 371 y cuánto falta aproximadamente; la barra de abajo muestra el porcentaje " +
                "con un anillo pequeño. Reconstruir el índice empieza de nuevo desde 0 % y " +
                "termina lista otra vez."
        )
    }
}

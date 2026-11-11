package app.notekea.autotest.cases.m2

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.expectLineSelected
import app.notekea.autotest.openVault
import app.notekea.autotest.pressInWindow
import app.notekea.autotest.waitForResult
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val HOME = "Home"
private const val REST_LINE =
    "Mix the flour, the water and the salt, then let it rest five minutes. ^dough"
private const val HOT_PAN_LINE = "Shape discs and cook them on a hot pan, five minutes a side."
private const val OPEN_SEARCH =
    "1. En Notekea, abre la Sample Vault (mira DATOS DE PRUEBA arriba) y espera a que la barra de abajo " +
        "diga \"41 notas listas para buscar\".\n2. Pulsa Ctrl+Shift+F (Cmd+Shift+F en macOS)."

/** The cases of 2.6, hybrid search (`MVP_SMOKE_TEST.md`): the palette's Content tab by meaning and keywords. */
fun SuiteBuilder.hybridSearchCases() {
    passageRows()
    byMeaning()
    nothingClose()
    exactWordsNotice()
}

private fun SuiteBuilder.passageRows() {
    automated(
        id = "2.6-01",
        title = "A search result names its heading and opens the note at the passage"
    ) {
        openVault(
            name = SAMPLE,
            shownText = HOME
        )

        pressInWindow(shortcut = Mod + Shift + Key.F)
        type(text = "rest five minutes")
        waitForResult(name = "Arepas")

        expectText(
            text = "Recipes › Steps",
            substring = true
        )

        press(key = Key.Enter)
        expectPassageSelected()
        pressInWindow(shortcut = Mod + Shift + Key.F)
        type(text = "cook them on a hot pan")
        waitForResult(name = "Arepas")
        press(key = Key.Enter)
        expectNoText(text = "Search the text of your notes")
        expectLineSelected(line = HOT_PAN_LINE)
    }
}

private fun CaseScope.expectPassageSelected() = expectLineSelected(line = REST_LINE)

private fun SuiteBuilder.byMeaning() {
    manual(
        id = "2.6-02",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)

        title(
            text =
            "Una búsqueda por significado encuentra una nota sin sus palabras, también en " +
                "otro idioma"
        )

        steps(
            text =
            "$OPEN_SEARCH\n3. Escribe: cómo hacer la masa\n4. Bórralo y escribe: trip to " +
                "Portugal"
        )

        expected(
            text =
            "La primera búsqueda muestra Arepas cerca de arriba con una línea sobre su masa, " +
                "aunque la nota no tiene ninguna de esas palabras. La segunda muestra primero " +
                "Trip to Lisbon."
        )
    }
}

private fun SuiteBuilder.nothingClose() {
    manual(
        id = "2.6-03",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)

        title(
            text =
            "Una búsqueda sin nada cercano no encuentra nada, en lugar de las notas más " +
                "cercanas"
        )

        steps(
            text =
            "$OPEN_SEARCH\n3. Escribe: football match results\n4. Bórralo y escribe: " +
                "football in the park"
        )

        expected(text = "La paleta dice \"Ninguna nota coincide\" las dos veces.")
    }
}

private fun SuiteBuilder.exactWordsNotice() {
    manual(
        id = "2.6-04",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "Mientras el índice no está listo, la búsqueda sigue funcionando y lo dice")

        steps(
            text =
            "$OPEN_SEARCH\n3. Pulsa Esc, haz clic en el estado del índice en la barra de " +
                "abajo y elige Reconstruir el índice.\n4. Mientras corre, pulsa Ctrl+Shift+F " +
                "(Cmd+Shift+F en macOS) y escribe: tomato"
        )

        expected(
            text =
            "Se muestran las notas con la palabra tomato, con \"Solo se muestran palabras " +
                "exactas hasta que tus notas estén listas para buscar.\" debajo. Cuando la barra " +
                "dice que las notas están listas, la línea desaparece."
        )
    }
}

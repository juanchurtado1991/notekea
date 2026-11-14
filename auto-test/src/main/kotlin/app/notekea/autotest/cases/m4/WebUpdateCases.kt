package app.notekea.autotest.cases.m4

import app.notekea.autotest.StandInDocsSite
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.openVault
import app.notekea.autotest.rightClickInTree
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val FOLDER = "Web sources"
private const val NOTE = "$PLAYGROUND/$FOLDER/Page 3.md"
private const val MINE = "My own line about page 3."

/**
 * Update from the web (the developer's request of 2026-11-13), on a site of the case's own: a clipped note whose page
 * changed gets the page's text again, under what the person wrote above it.
 */
internal fun SuiteBuilder.webUpdateCases() {
    automated(
        id = "4.4-05",
        title = "Update from the web, in the menu of Web sources, rewrites a clipped note whose page changed"
    ) {
        val page = StandInDocsSite.ADDRESS.replace(
            oldValue = "intro",
            newValue = "page-3"
        )

        Files.createDirectories(file(path = "$PLAYGROUND/$FOLDER"))

        Files.writeString(
            file(path = NOTE),
            "---\nsource: \"$page\"\nclipped: 2026-10-01\n---\n\n# Page 3\n\n$MINE\n\n## Original text\n\nAs it was.\n"
        )

        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        rightClickInTree(text = FOLDER)
        click(text = "Update from the web")
        expectText(text = "1 updated, 0 new, 0 unchanged.")
        click(text = "Close")

        expectFile(
            path = NOTE,
            containing = "Page 3 explains one part of the guide"
        )

        expectFile(
            path = NOTE,
            containing = MINE
        )
    }
}

// The developer's request of 2026-11-13: the question says how deep, and the chat says how far it is.
internal fun SuiteBuilder.clipDepthCases() {
    manual(
        id = "4.4-06",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "El chat pregunta a qué profundidad seguir los enlaces de una documentación y " +
                "dice cuánto lleva"
        )

        steps(
            text =
            "1. Con un modelo de chat instalado, abre la Playground Vault y el chat.\n2. " +
                "Pide: clip https://wordpress.org/documentation/\n3. En la pregunta, elige Ir más " +
                "profundo.\n4. Cuando diga cuántas páginas encontró, elige Recortar N páginas.\n5. " +
                "Mira el chat mientras trabaja y, al terminar, haz clic en Hacerlo.\n6. Pide lo " +
                "mismo otra vez y elige Documentación completa."
        )

        expected(
            text =
            "La pregunta dice cuántos enlaces encontró y lista cuatro filas, cada una con qué " +
                "recorta y cuánto: Solo esta página, Esta página y sus enlaces, Ir más profundo y " +
                "Documentación completa, con la que encaja marcada Recomendada. Una opción profunda " +
                "sigue primero los enlaces (el chat dice cuántas páginas lleva), dice cuántas " +
                "encontró y vuelve a preguntar antes de recortar. Luego dice Recortando la página " +
                "n de m, con una barra. Cada nota guarda el texto con su forma (títulos, listas, " +
                "código, tablas), sin el menú del sitio."
        )
    }
}

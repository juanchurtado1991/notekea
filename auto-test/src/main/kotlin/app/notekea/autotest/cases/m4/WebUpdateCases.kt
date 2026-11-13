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
                "Pide: clip https://developer.android.com/develop\n3. En la pregunta, elige 2 " +
                "niveles.\n4. Mira el chat mientras trabaja y, al terminar, haz clic en Hacerlo."
        )

        expected(
            text =
            "La pregunta ofrece Solo esta página, Esta página y N más, y 2 o 3 niveles. " +
                "Mientras trabaja, el chat dice cuántas páginas dieron los enlaces hasta ahora y " +
                "luego Recortando la página n de m, con una barra. Al final hay notas de las " +
                "páginas de dos niveles en Web sources."
        )
    }
}

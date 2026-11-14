package app.notekea.autotest.cases.m4

import app.notekea.autotest.StandInDocsSite
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.openVault
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val GUIDE = "Field guide.html"
private const val GUIDE_HEADING = "Birds of the river"
private const val GUIDE_SENTENCE = "The kingfisher nests in the clay banks above the water."
private const val CARDS = "Capitals.md"
private const val QUESTION = "Capital of Portugal"
private const val ANSWER = "Lisbon"

/**
 * The cases of M4b without a model (`MVP_SMOKE_TEST.md`): a document in the vault opens read-only (4.5), a clip of an
 * address on this computer is refused before anything is downloaded (4.4), and study cards written in a note are
 * reviewed in Study cards with their next date written back (4.6).
 */
fun SuiteBuilder.sourceCases() {
    document()
    pdfPages()
    localClip()
    clipOfPage()
    clipOfSection()
    allOfSection()
    review()
    madeCards()
    studyChoiceCases()
    webUpdateCases()
    clipDepthCases()
    lookFurtherCases()
}

private fun SuiteBuilder.document() {
    automated(
        id = "4.5-01",
        title = "A document in the vault is listed in Files and opens read-only as its text"
    ) {
        Files.writeString(
            file(path = "$PLAYGROUND/$GUIDE"),
            "<html><head><title>Field guide</title></head><body><h1>$GUIDE_HEADING</h1>" +
                "<p>$GUIDE_SENTENCE</p></body></html>"
        )

        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        click(text = GUIDE)
        expectText(text = GUIDE_HEADING)
        expectText(text = GUIDE_SENTENCE)
        expectText(text = "Open in the system’s app")

        expectFile(
            path = "$PLAYGROUND/$GUIDE",
            containing = GUIDE_SENTENCE
        )
    }
}

private fun SuiteBuilder.pdfPages() {
    manual(
        id = "4.5-02",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "Un PDF se abre con sus páginas reales, y la cita del chat abre la página " +
                "citada, marcada"
        )

        steps(
            text =
            "1. Copia cualquier PDF con texto (un artículo, un manual) de unas pocas páginas " +
                "en la carpeta de la Playground Vault.\n2. Abre la Playground Vault en Notekea y " +
                "haz clic en el PDF en Archivos.\n3. Abre el chat (el globo de diálogo en la " +
                "barra estrecha de la izquierda) y haz una pregunta que responda una frase de la " +
                "tercera página del PDF.\n4. Haz clic en la cita del PDF bajo la respuesta."
        )

        expected(
            text =
            "El paso 2 muestra las páginas del PDF como se imprimen, con Abrir en la app del " +
                "sistema encima. El paso 3 cita el PDF y su página. El paso 4 abre el PDF en esa " +
                "página con el pasaje citado marcado en el color del tema."
        )
    }
}

private fun SuiteBuilder.localClip() {
    automated(
        id = "4.4-01",
        title = "Clip a web page refuses an address on this computer before downloading anything"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        click(text = "Clip a web page")
        click(text = "https://example.com/an-article")
        type(text = "http://localhost:8080/secrets")
        click(text = "Clip")
        expectText(text = "Notekea never opens addresses on this computer or its local network.")
        expectNoFile(path = "$PLAYGROUND/Web sources")
    }
}

private fun SuiteBuilder.clipOfPage() {
    manual(
        id = "4.4-02",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "Un artículo web se recorta con notas que el artículo respalda, y una página que " +
                "no lo es dice por qué"
        )

        steps(
            text =
            "1. Abre la Playground Vault con un modelo de chat instalado (Ajustes → Modelo " +
                "de IA).\n2. Cierra todas las notas, haz clic en Recortar una página web y pega " +
                "la dirección de un artículo de noticias o de un blog; haz clic en Recortar.\n3. " +
                "Hazlo de nuevo con https://www.youtube.com.\n4. En el chat, escribe clip " +
                "seguido de la dirección de otro artículo."
        )

        expected(
            text =
            "El paso 2 dice Descargando la página…, Escribiendo notas sobre ella… y " +
                "Revisando las notas contra el artículo…, y luego abre Web sources/<el título " +
                "del artículo> con las propiedades source y clipped, notas sobre el artículo y " +
                "su texto bajo Original text. El paso 3 dice que no se encontró un artículo y no " +
                "escribe nada. El paso 4 muestra la tarjeta de siempre y recorta la página " +
                "cuando haces clic en Do it."
        )
    }
}

// The developer's request of 2026-11-11: a documentation page asks whether to clip the pages it links too.
private fun SuiteBuilder.clipOfSection() {
    manual(
        id = "4.4-03",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "Una página de documentación pregunta si recortar también las páginas que enlaza " +
                "bajo su ruta"
        )

        steps(
            text =
            "1. Abre la Playground Vault con un modelo de chat instalado.\n2. Cierra todas " +
                "las notas, haz clic en Recortar una página web y pega " +
                "https://www.traceloop.com/docs/openllmetry/integrations/introduction; haz clic " +
                "en Recortar.\n3. Haz clic en Esta página y 30 más y espera.\n4. En el chat (el " +
                "globo de diálogo de la izquierda) escribe clip y la misma dirección, y luego " +
                "haz clic en Only this page."
        )

        expected(
            text =
            "El paso 2 pregunta: solo esta página, o esta página y 30 más, con 2 más fuera. " +
                "El paso 3 cuenta Recortando la página n de 31 y luego abre Web " +
                "sources/Overview, cuya lista Pages enlaza la nota de cada integración en Web " +
                "sources/Overview/. El paso 4 pregunta lo mismo con dos botones; Only this page " +
                "muestra una tarjeta con una nota."
        )
    }
}

// The developer's request of 2026-11-11: thirty pages are the default, all of them when asked; on a site of the case's
// own, so no network is needed.
private fun SuiteBuilder.allOfSection() {
    automated(
        id = "4.4-04",
        title = "A documentation page offers its closest thirty pages or all of them, and All clips every one"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        click(text = "Clip a web page")
        click(text = "https://example.com/an-article")
        type(text = StandInDocsSite.ADDRESS)
        click(text = "Clip")
        expectText(text = "This page and 30 more")
        click(text = "All ${StandInDocsSite.PAGES} pages")

        expectFile(
            path = "$PLAYGROUND/Web sources/Intro/Page ${StandInDocsSite.PAGES}.md",
            containing = "Page ${StandInDocsSite.PAGES} explains"
        )

        expectFile(
            path = "$PLAYGROUND/Web sources/Intro.md",
            containing = "[[Intro/Page 1|Page 1]]"
        )

        expectFile(
            path = "$PLAYGROUND/Web sources/Intro.md",
            containing = "[[Intro/Page ${StandInDocsSite.PAGES}|Page ${StandInDocsSite.PAGES}]]"
        )
    }
}

private fun SuiteBuilder.review() {
    automated(
        id = "4.6-01",
        title = "Study cards written in a note are reviewed, and Good writes the next date in the note"
    ) {
        Files.writeString(file(path = "$PLAYGROUND/$CARDS"), "# Capitals\n\n#flashcards\n\n$QUESTION::$ANSWER\n")

        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Study cards")

        expectText(
            text = QUESTION,
            substring = true
        )

        expectNoText(text = ANSWER)
        click(text = "Show answer")

        expectText(
            text = ANSWER,
            substring = true
        )

        click(text = "Good")

        expectText(
            text = "All done for today",
            substring = true
        )

        expectFile(
            path = "$PLAYGROUND/$CARDS",
            containing = "<!--SR:!"
        )
    }
}

private fun SuiteBuilder.madeCards() {
    manual(
        id = "4.6-02",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "El chat crea tarjetas de estudio de una nota, y se repasan en Tarjetas de " +
                "estudio"
        )

        steps(
            text =
            "1. Con un modelo de chat instalado (Ajustes → Modelo de IA), abre " +
                "Recipes/Arepas en la Playground Vault.\n2. Abre el chat (el globo de diálogo en " +
                "la barra estrecha de la izquierda) y pide: make study cards from this note.\n3. " +
                "Haz clic en Do it en la tarjeta.\n4. Haz clic en Tarjetas de estudio (el " +
                "birrete en la barra estrecha)."
        )

        expected(
            text =
            "La tarjeta del paso 2 muestra preguntas que la nota responde, cada una con su " +
                "respuesta. El paso 3 las agrega al final de Arepas bajo un encabezado " +
                "Flashcards con #flashcards. El paso 4 las muestra una a una."
        )
    }
}

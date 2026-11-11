package app.notekea.autotest.cases.m4

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
    review()
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
        title(text = "A PDF opens as its real pages, and the chat's citation of it opens the cited page, marked")

        steps(
            text =
            "1. Copy any PDF with text (a paper, a manual) of a few pages into the Playground Vault's folder.\n" +
                "2. Open the Playground Vault in Notekea and click the PDF in Files.\n3. Open the chat (the " +
                "speech bubble in the narrow bar on the far left) and ask a question a sentence of the PDF's " +
                "third page answers.\n4. Click the citation of the PDF under the answer."
        )

        expected(
            text =
            "Step 2 shows the PDF's pages as they print, with Open in the system's app above them. Step 3 cites " +
                "the PDF and its page. Step 4 opens the PDF at that page with the cited passage marked in the " +
                "theme's color."
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
        title(text = "A web article is clipped with notes the article backs, and a page that is not one says why")

        steps(
            text =
            "1. Open the Playground Vault with a chat model installed (Settings → AI model).\n2. Close every " +
                "note, click Clip a web page and paste the address of a news or blog article; click Clip.\n3. " +
                "Do it again with https://www.youtube.com.\n4. In the chat, write clip followed by another " +
                "article's address."
        )

        expected(
            text =
            "Step 2 says Downloading the page…, Writing notes about it… and Checking the notes…, then opens " +
                "Web sources/<the article's title> with source and clipped properties, notes about the article and " +
                "its text under Original text. Step 3 says no article was found and writes nothing. Step 4 shows " +
                "the usual card and clips the page once you click Do it."
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

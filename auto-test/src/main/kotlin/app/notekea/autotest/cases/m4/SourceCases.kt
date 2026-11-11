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

// The developer's request of 2026-11-11: a documentation page asks whether to clip the pages it links too.
private fun SuiteBuilder.clipOfSection() {
    manual(
        id = "4.4-03",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "A documentation page asks whether to clip the pages it links under its path too")

        steps(
            text =
            "1. Open the Playground Vault with a chat model installed.\n2. Close every note, click Clip a web " +
                "page and paste https://www.traceloop.com/docs/openllmetry/integrations/introduction; click Clip." +
                "\n3. Click This page and 30 more and wait.\n4. In the chat (the speech bubble on the far left) " +
                "write clip and the same address, then click Only this page."
        )

        expected(
            text =
            "Step 2 asks: only this page, or this page and 30 more, with 2 more left out. Step 3 counts Clipping " +
                "page n of 31, then opens Web sources/Overview, whose Pages list links each integration's note in " +
                "Web sources/Overview/. Step 4 asks the same with two buttons; Only this page shows a card with one " +
                "note."
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
        title(text = "The chat makes study cards from a note, and they are reviewed in Study cards")

        steps(
            text =
            "1. With a chat model installed (Settings → AI model), open Recipes/Arepas in the Playground Vault.\n" +
                "2. Open the chat (the speech bubble in the narrow bar on the far left) and ask: make study cards " +
                "from this note.\n3. Click Do it on the card.\n4. Click Study cards (the cap in the narrow bar)."
        )

        expected(
            text =
            "Step 2's card lists questions the note answers, each with its answer. Step 3 adds them at the end of " +
                "Arepas under a Flashcards heading with #flashcards. Step 4 shows them one at a time."
        )
    }
}

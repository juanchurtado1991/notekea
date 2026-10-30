package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import app.notekea.selftest.notekea
import app.notekea.selftest.openNote
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.relaunch
import com.testbuddy.CaseFailure
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val READ_MODE = "Read mode"
private const val RAW_BOLD = "Some **bold**"
private const val OPEN_READ_MODE =
    "1. In Notekea, open the Playground Vault from the Test data folder (Open folder…, after Open another vault at the bottom left if a vault is open).\n" +
        "2. Open the note Editor › Read mode."

/** The cases of 1.5 that show a note: how it looks, finding in it, Preview and its links (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.editorViewCases() {
    editLook()
    findInNote()
    previewRemembered()
    previewLinks()
    previewLook()
}

private fun SuiteBuilder.editLook() {
    manual(id = "1.5-08", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "How a note looks while you edit it")
        steps(text = "$OPEN_READ_MODE\n3. Stay in Edit (the Edit tab under the title) and look at the text.")
        expected(
            text = "Headings are larger, bold and italic show as such, code uses a monospaced font on a shaded " +
                "background, links are blue, tags are orange, the Markdown signs (#, **, [[) are muted, and the two " +
                "tasks show a checkbox."
        )
    }
}

private fun SuiteBuilder.findInNote() {
    automated(id = "1.5-09", title = "Finding in a note") {
        openPlaygroundNote(name = READ_MODE, shownText = RAW_BOLD)
        press(shortcut = Mod + Key.F)
        type(text = "arepas")
        expectText(text = "1 of 2")
        press(key = Key.Enter)
        expectText(text = "2 of 2")
        press(key = Key.Enter)
        expectText(text = "1 of 2")
        press(shortcut = Shift + Key.Enter)
        expectText(text = "2 of 2")
        press(key = Key.Escape)
        expectNoText(text = "2 of 2")
    }
}

private fun SuiteBuilder.previewRemembered() {
    automated(id = "1.5-10", title = "Preview, and the mode is remembered") {
        openPlaygroundNote(name = READ_MODE, shownText = RAW_BOLD)
        press(shortcut = Mod + Key.E)
        expectNoText(text = RAW_BOLD, substring = true)
        expectText(text = "A finished task", substring = true)
        openNote(name = "Arepas", shownText = "2 cups pre-cooked white corn flour")
        expectNoText(text = "## Ingredients", substring = true)
        relaunch()
        expectText(text = "2 cups pre-cooked white corn flour", substring = true)
        expectNoText(text = "## Ingredients", substring = true)
        pressInWindow(shortcut = Mod + Key.E)
        expectText(text = "## Ingredients", substring = true)
    }
}

private fun SuiteBuilder.previewLinks() {
    automated(id = "1.5-11", title = "Links in Preview open notes, make missing ones and open the web") {
        openPlaygroundNote(name = READ_MODE, shownText = RAW_BOLD)
        press(shortcut = Mod + Key.E)
        // Wait for Preview before clicking a link: until it shows, the words are the editor's text, not links.
        expectNoText(text = RAW_BOLD, substring = true)
        clickWords(words = "Arepas")
        expectText(text = "2 cups pre-cooked white corn flour", substring = true)
        openNote(name = READ_MODE, shownText = "A finished task")
        clickWords(words = "the coffee")
        expectText(text = "Half strong coffee", substring = true)
        openNote(name = READ_MODE, shownText = "A finished task")
        clickWords(words = "A note that does not exist yet")
        expectFile(path = "$PLAYGROUND/A note that does not exist yet.md")
        openNote(name = READ_MODE, shownText = "A finished task")
        clickWords(words = "Pasta e fagioli")
        expectText(text = "Pasta e fagioli")
        openNote(name = READ_MODE, shownText = "A finished task")
        clickWords(words = "the web")
        ui.waitUntil(timeoutMillis = 10_000) { "https://example.com" in notekea().uriHandler.opened }
        val codeLink = "not a link inside code"
        if (notekea().uriHandler.opened.any {
                codeLink in it
            }
        ) {
            throw CaseFailure(message = "expected no link inside code")
        }
    }
}

private fun SuiteBuilder.previewLook() {
    manual(id = "1.5-12", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "How a note looks in Preview")
        steps(text = "$OPEN_READ_MODE\n3. Click Preview under the title and look at the note.")
        expected(
            text = "Headings, lists, checkboxes and the quote are drawn as such, and both code blocks are colored " +
                "by their language (Kotlin and Python)."
        )
    }
}

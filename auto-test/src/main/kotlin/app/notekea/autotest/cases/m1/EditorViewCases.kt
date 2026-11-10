package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.cursorToNoteStart
import app.notekea.autotest.notekea
import app.notekea.autotest.openNote
import app.notekea.autotest.pressInWindow
import app.notekea.autotest.relaunch
import com.testbuddy.CaseFailure
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val READ_MODE = "Read mode"
private const val RAW_BOLD = "Some **bold**"
private const val DRAWN_TASK = "A task to do"
private const val OPEN_READ_MODE =
    "1. In Notekea, open the Playground Vault (see TEST DATA above).\n2. Open the note Editor › Read mode."

/** The cases of 1.5 that show a note: how it looks, finding in it, Source and its links (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.editorViewCases() {
    editLook()
    findInNote()
    sourceAndBack()
    noteLinks()
    readingLook()
    createdKept()
}

private fun SuiteBuilder.editLook() {
    manual(
        id = "1.5-08",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "The line the cursor is on shows its Markdown signs")

        steps(
            text = "$OPEN_READ_MODE\n3. Click the words Some bold in the note and look at that line and at the " +
                "others.\n4. Click Source (</>) at the end of the toolbar, look, and click it again."
        )

        expected(
            text =
            "The clicked line shows its Markdown signs (**, *, `, #) muted, with bold and italic as such, and the " +
                "toolbar marks Bold (B) in blue while the cursor is in the bold word; every other line stays as " +
                "it reads. Source shows the whole note as written, with the signs muted, links " +
                "and tags blue and code on a shaded background; the second click draws the note again."
        )
    }
}

private fun SuiteBuilder.findInNote() {
    automated(
        id = "1.5-09",
        title = "Finding in a note"
    ) {
        openPlaygroundNote(
            name = READ_MODE,
            shownText = RAW_BOLD
        )

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

private fun SuiteBuilder.sourceAndBack() {
    automated(
        id = "1.5-10",
        title = "Source shows the note as written, and its button draws it again"
    ) {
        openPlaygroundNote(
            name = READ_MODE,
            shownText = RAW_BOLD
        )

        // A task drawn in a block the cursor is not in is a text of its own; as written it is part of the note's.
        expectText(text = DRAWN_TASK)
        pressInWindow(shortcut = Mod + Key.E)
        expectNoText(text = DRAWN_TASK)
        clickIcon(label = "Source: the note as written")
        expectText(text = DRAWN_TASK)
    }
}

private fun SuiteBuilder.noteLinks() {
    automated(
        id = "1.5-11",
        title = "Links open notes, make missing ones and open the web"
    ) {
        openPlaygroundNote(
            name = READ_MODE,
            shownText = RAW_BOLD
        )

        // In the cursor's block the words are the editor's text, not links.
        cursorToNoteStart()

        clickWords(words = "Arepas")

        expectText(
            text = "2 cups pre-cooked white corn flour",
            substring = true
        )

        openNote(
            name = READ_MODE,
            shownText = "A finished task"
        )

        clickWords(words = "the coffee")

        expectText(
            text = "Half strong coffee",
            substring = true
        )

        openNote(
            name = READ_MODE,
            shownText = "A finished task"
        )

        clickWords(words = "A note that does not exist yet")
        expectFile(path = "$PLAYGROUND/A note that does not exist yet.md")

        openNote(
            name = READ_MODE,
            shownText = "A finished task"
        )

        clickWords(words = "in a new folder")
        expectFile(path = "$PLAYGROUND/Drafts/A note in a folder.md")

        openNote(
            name = READ_MODE,
            shownText = "A finished task"
        )

        clickWords(words = "Pasta e fagioli")
        expectText(text = "Pasta e fagioli")

        openNote(
            name = READ_MODE,
            shownText = "A finished task"
        )

        clickWords(words = "the web")

        ui
            .waitUntil(timeoutMillis = 10_000) {
                "https://example.com" in notekea().uriHandler.opened
            }

        val codeLink = "not a link inside code"

        if (notekea().uriHandler.opened.any {
                codeLink in it
            }
        ) {
            throw CaseFailure(message = "expected no link inside code")
        }
    }
}

private fun SuiteBuilder.readingLook() {
    manual(
        id = "1.5-12",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "How a note reads")
        steps(text = "$OPEN_READ_MODE\n3. Look at the note without clicking in its text.")

        expected(
            text =
            "Headings, lists, checkboxes and the quote are drawn as such, and both code blocks are colored by " +
                "their language (Kotlin and Python)."
        )
    }
}

private fun SuiteBuilder.createdKept() {
    manual(
        id = "1.5-13",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Saving a note keeps the date it was created")

        steps(
            text = "$OPEN_READ_MODE\n3. Look at Created under Note info in the side panel.\n" +
                "4. Type a word in the note, wait until it says Saved, then open another note and come back."
        )

        expected(
            text =
            "On Windows and macOS, Created shows the same date and time as before. Linux cannot keep it, so " +
                "this case does not apply there."
        )
    }
}

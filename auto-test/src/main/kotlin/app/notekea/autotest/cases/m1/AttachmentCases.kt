package app.notekea.autotest.cases.m1

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.compose.ui.text.TextRange
import app.notekea.autotest.clickInTree
import app.notekea.autotest.cursorToNoteStart
import app.notekea.autotest.noteText
import app.notekea.autotest.openVault
import app.notekea.autotest.renameInTree
import com.testbuddy.CaseScope
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val EMBED_TRIES = 10
private const val EMBED_WAIT_MILLIS = 1_000L
private const val IMAGES_NOTE = "Images and files"
private const val IMAGES_FILE = "$PLAYGROUND/Editor/Images and files.md"
private const val IMAGES_SHOWN = "A picture at its own size"

/** The cases of 1.8 the program checks: embeds as text, the attachments folder and notes shown in a note. */
fun SuiteBuilder.attachmentCases() {
    embedsInEdit()
    attachmentFiles()
    embeddedNotes()
    attachmentSystemCases()
    attachmentManualCases()
}

private fun SuiteBuilder.embedsInEdit() {
    automated(
        id = "1.8-02",
        title = "Embed lines keep their text and move with it"
    ) {
        openPlaygroundNote(
            name = IMAGES_NOTE,
            shownText = IMAGES_SHOWN
        )

        val original = Files.readString(file(path = IMAGES_FILE))

        listOf("![[Arepas plate.png|200]]", "![[Weekly menu.pdf]]", "![[Lost photo.png]]").forEach {
            expectText(
                text = it,
                substring = true
            )
        }

        val field = noteText(part = IMAGES_SHOWN)
        field.performTextInputSelection(selection = TextRange(index = 0))
        field.performTextInput(text = "A line at the top.\n")

        expectFile(
            path = IMAGES_FILE,
            content = "A line at the top.\n$original"
        )

        val body = field
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text

        if (!body.startsWith("A line at the top.")) {
            throw com.testbuddy.CaseFailure(message = "expected the new line first")
        }
    }
}

private fun SuiteBuilder.attachmentFiles() {
    automated(
        id = "1.8-12",
        title = "The attachments folder lists every file, each opening in a tab"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickInTree(text = "attachments")

        listOf(
            "Arepas plate.png",
            "Weekly menu.pdf",
            "broken.png",
            "shopping.txt",
            "Market photo.jpg"
        ).forEach { expectText(text = it) }

        clickInTree(text = "Arepas plate.png")

        expectText(
            text = "Image · 480 × 240",
            substring = true
        )

        clickInTree(text = "Weekly menu.pdf")
        // A PDF opens as its pages (4.5), with the system's app one click away.
        expectText(text = "Open in the system’s app")
        clickInTree(text = "broken.png")

        expectText(
            text = "Notekea can’t show this file here.",
            substring = true
        )

        renameInTree(
            name = "shopping.txt",
            newName = "groceries.txt"
        )

        expectFile(path = "$PLAYGROUND/attachments/groceries.txt")
        expectNoFile(path = "$PLAYGROUND/attachments/shopping.txt")
    }
}

private fun SuiteBuilder.embeddedNotes() {
    automated(
        id = "1.8-13",
        title = "A note, and a section of it, shown inside another note"
    ) {
        openPlaygroundNote(
            name = IMAGES_NOTE,
            shownText = IMAGES_SHOWN
        )

        cursorToNoteStart()

        scrollToEmbedded(text = "What the kitchen always needs.")

        expectText(
            text = "What the kitchen always needs.",
            substring = true
        )

        scrollTo(text = "Shopping basics › Dairy")
        expectText(text = "Shopping basics › Dairy")

        expectNoText(
            text = "tags: [shopping]",
            substring = true
        )

        click(text = "Shopping basics › Dairy")

        ui.waitUntilAtLeastOneExists(
            matcher = hasSetTextAction() and hasText(text = "Shopping basics"),
            timeoutMillis = 10_000
        )
    }
}

/**
 * Scrolls to [text] of a note embedded in the open one. The embedded note is read after the note that holds it, and
 * until then no list holds its text (macOS's runner failed 1.8-13 that way): the scroll is tried again for a while.
 */
private fun CaseScope.scrollToEmbedded(text: String) {
    repeat(times = EMBED_TRIES - 1) {
        val isFound = runCatching {
            scrollTo(
                text = text,
                substring = true
            )
        }.isSuccess

        if (isFound) return
        settle(millis = EMBED_WAIT_MILLIS)
    }

    scrollTo(
        text = text,
        substring = true
    )
}

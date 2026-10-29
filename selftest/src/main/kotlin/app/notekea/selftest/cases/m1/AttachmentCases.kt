package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import app.notekea.selftest.clickInTree
import app.notekea.selftest.noteText
import app.notekea.selftest.openVault
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.renameInTree
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val IMAGES_NOTE = "Images and files"
private const val IMAGES_FILE = "$PLAYGROUND/Editor/Images and files.md"
private const val IMAGES_SHOWN = "A picture at its own size"

/** The cases of 1.8 the program checks: embeds in Edit, the attachments folder and notes shown in a note. */
fun SuiteBuilder.attachmentCases() {
    embedsInEdit()
    attachmentFiles()
    embeddedNotes()
    attachmentManualCases()
}

private fun SuiteBuilder.embedsInEdit() {
    automated(id = "1.8-02", title = "Embed lines keep their text in Edit and move with it") {
        openPlaygroundNote(name = IMAGES_NOTE, shownText = IMAGES_SHOWN)
        val original = Files.readString(file(path = IMAGES_FILE))
        listOf("![[Arepas plate.png|200]]", "![[Weekly menu.pdf]]", "![[Lost photo.png]]").forEach {
            expectText(text = it, substring = true)
        }
        val field = noteText(part = IMAGES_SHOWN)
        field.performTextInputSelection(selection = TextRange(index = 0))
        field.performTextInput(text = "A line at the top.\n")
        expectFile(path = IMAGES_FILE, content = "A line at the top.\n$original")
        val body = field.fetchSemanticsNode().config[SemanticsProperties.EditableText].text
        if (!body.startsWith(
                "A line at the top."
            )
        ) {
            throw com.testbuddy.CaseFailure(message = "expected the new line first")
        }
    }
}

private fun SuiteBuilder.attachmentFiles() {
    automated(id = "1.8-12", title = "The attachments folder lists every file, each opening in a tab") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = "attachments")
        listOf("Arepas plate.png", "Weekly menu.pdf", "broken.png", "shopping.txt", "Market photo.jpg").forEach {
            expectText(text = it)
        }
        clickInTree(text = "Arepas plate.png")
        expectText(text = "Image · 480 × 240", substring = true)
        clickInTree(text = "Weekly menu.pdf")
        expectText(text = "Open with its app")
        clickInTree(text = "broken.png")
        expectText(text = "Notekea can’t show this file here.", substring = true)
        renameInTree(name = "shopping.txt", newName = "groceries.txt")
        expectFile(path = "$PLAYGROUND/attachments/groceries.txt")
        expectNoFile(path = "$PLAYGROUND/attachments/shopping.txt")
    }
}

private fun SuiteBuilder.embeddedNotes() {
    automated(id = "1.8-13", title = "A note, and a section of it, shown inside another note") {
        openPlaygroundNote(name = IMAGES_NOTE, shownText = IMAGES_SHOWN)
        pressInWindow(shortcut = Mod + Key.E)
        expectText(text = "What the kitchen always needs.", substring = true)
        expectText(text = "Shopping basics › Dairy")
        expectNoText(text = "tags: [shopping]", substring = true)
        pressInWindow(shortcut = Mod + Key.E)
        expectText(text = "![[Shopping basics#Dairy]]", substring = true)
        expectText(text = "Shopping basics › Dairy")
        // Clicked in Preview, which scrolls to the frame as a person would.
        pressInWindow(shortcut = Mod + Key.E)
        click(text = "Shopping basics › Dairy")
        ui.waitUntilAtLeastOneExists(
            matcher = hasSetTextAction() and hasText(text = "Shopping basics"),
            timeoutMillis = 10_000
        )
    }
}

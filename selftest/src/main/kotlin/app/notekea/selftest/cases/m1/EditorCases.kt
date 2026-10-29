package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import app.notekea.selftest.SmokeScenarios
import app.notekea.selftest.clickInTree
import app.notekea.selftest.openNote
import app.notekea.selftest.openVault
import app.notekea.selftest.relaunch
import app.notekea.selftest.titleField
import app.notekea.selftest.typeAtEnd
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val AREPAS_TEXT = "2 cups pre-cooked white corn flour"
private const val SHARED = "Shared note"
private const val SHARED_TEXT = "Another app changes or deletes this note"
private const val SHARED_FILE = "$PLAYGROUND/Editor/Shared note.md"
private const val OUTSIDE_PAUSE_MILLIS = 1_500L

/** The cases of 1.5 that write and save notes (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.editorCases() {
    typingIsSaved()
    savedOnLeaving()
    windowsLineEndings()
    renameFromTitle()
    outsideEdit()
    conflictWhileTyping()
    deletedOutside()
}

private fun SuiteBuilder.typingIsSaved() {
    automated(id = "1.5-01", title = "Typing is saved, the frontmatter as it was") {
        openPlaygroundNote(name = "Arepas", shownText = AREPAS_TEXT)
        typeAtEnd(part = AREPAS_TEXT, text = "\nServe them warm.")
        expectText(text = "Unsaved")
        expectText(text = "Saved")
        expectFile(path = "$PLAYGROUND/Recipes/Arepas.md", containing = "Serve them warm.")
        expectFile(
            path = "$PLAYGROUND/Recipes/Arepas.md",
            containing = "---\ntags: [recipe, venezuelan]\naliases: [Arepa, Corn cakes]\nservings: 6\n---\n"
        )
    }
}

private fun SuiteBuilder.savedOnLeaving() {
    automated(id = "1.5-02", title = "Mod+S, leaving the note and quitting all save it") {
        openPlaygroundNote(name = "Arepas", shownText = AREPAS_TEXT)
        typeAtEnd(part = AREPAS_TEXT, text = "\nFirst line.")
        press(shortcut = Mod + Key.S)
        expectFile(path = "$PLAYGROUND/Recipes/Arepas.md", containing = "First line.")
        typeAtEnd(part = AREPAS_TEXT, text = "\nSecond line.")
        clickInTree(text = WELCOME)
        expectFile(path = "$PLAYGROUND/Recipes/Arepas.md", containing = "Second line.")
        clickInTree(text = "Arepas")
        typeAtEnd(part = AREPAS_TEXT, text = "\nThird line.")
        relaunch()
        expectFile(path = "$PLAYGROUND/Recipes/Arepas.md", containing = "Third line.")
    }
}

private fun SuiteBuilder.windowsLineEndings() {
    automated(id = "1.5-03", title = "A note with Windows line endings keeps them") {
        openPlaygroundNote(name = "Windows line endings", shownText = "Every line ends with CR LF.")
        typeAtEnd(part = "Every line ends with CR LF.", text = "\nOne more line.")
        val note = "$PLAYGROUND/Editor/Windows line endings.md"
        expectFile(path = note, containing = "One more line.")
        val text = Files.readString(file(path = note))
        val lines = text.split("\n").dropLast(1)
        val header = "---\r\ntitle: Written on Windows\r\ntags: [crlf]\r\n---\r\n"
        if (lines.any { !it.endsWith("\r") } || !text.startsWith(header)) {
            throw CaseFailure(message = "expected every line to end with CR LF and the frontmatter unchanged")
        }
    }
}

private fun SuiteBuilder.renameFromTitle() {
    automated(id = "1.5-04", title = "Renaming a note from its title") {
        openPlaygroundNote(name = "Arepas", shownText = AREPAS_TEXT)
        renameFromTitle(name = "Arepas", newName = "Arepas reina")
        expectText(text = "Links updated in 3 notes: 2026-10-20, Read mode, Welcome.")
        expectFile(path = "$PLAYGROUND/Recipes/Arepas reina.md")
        renameFromTitle(name = "Arepas reina", newName = "a/b")
        expectText(text = "Enter a name without characters like / : * ? or quotes.")
        press(key = Key.Escape)
        expectText(text = "Arepas reina")
    }
}

private fun SuiteBuilder.outsideEdit() {
    automated(id = "1.5-05", title = "A change made by another app shows at once") {
        openPlaygroundNote(name = SHARED, shownText = SHARED_TEXT)
        scenarios().editNote(time = "10:00:00")
        expectText(text = "Edited outside Notekea at 10:00:00.", substring = true)
    }
}

private fun SuiteBuilder.conflictWhileTyping() {
    automated(id = "1.5-06", title = "A change by another app while typing asks which to keep") {
        openPlaygroundNote(name = SHARED, shownText = SHARED_TEXT)
        typeAtEnd(part = SHARED_TEXT, text = "\nMine.")
        scenarios().editNote(time = "10:00:00")
        expectText(text = "This note changed outside Notekea while you were editing it.")
        click(text = "Keep mine")
        expectFile(path = SHARED_FILE, containing = "Mine.")
        typeAtEnd(part = SHARED_TEXT, text = "\nMine again.")
        scenarios().editNote(time = "11:00:00")
        expectText(text = "This note changed outside Notekea while you were editing it.")
        click(text = "Load theirs")
        expectText(text = "Edited outside Notekea at 11:00:00.", substring = true)
        expectNoText(text = "Mine again.", substring = true)
    }
}

private fun SuiteBuilder.deletedOutside() {
    automated(id = "1.5-07", title = "A note deleted by another app can be restored or closed") {
        openPlaygroundNote(name = SHARED, shownText = SHARED_TEXT)
        scenarios().deleteNote()
        expectText(text = "This note was moved or deleted outside Notekea.")
        click(text = "Restore")
        expectFile(path = SHARED_FILE, containing = SHARED_TEXT)
        // Notekea looks at an open note once a second; a person takes longer than that before the next change.
        Thread.sleep(OUTSIDE_PAUSE_MILLIS)
        scenarios().deleteNote()
        expectText(text = "This note was moved or deleted outside Notekea.")
        click(text = "Close")
        expectNoText(text = SHARED)
    }
}

/** Opens the Playground Vault and its note [name] with the quick switcher. */
internal fun CaseScope.openPlaygroundNote(
    name: String,
    shownText: String
) {
    openVault(name = PLAYGROUND, shownText = WELCOME)
    openNote(name = name, shownText = shownText)
}

private fun CaseScope.renameFromTitle(
    name: String,
    newName: String
) {
    titleField(name = name).apply {
        performClick()
        performTextReplacement(text = newName)
    }
    // The field is drawn anew with the new text, so it is found again to confirm it.
    titleField(name = newName).performKeyInput { pressKey(key = Key.Enter) }
}

private fun CaseScope.scenarios() = SmokeScenarios(smokeData = folders.data, scratch = folders.data)

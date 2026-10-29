package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import app.notekea.selftest.clickInTree
import app.notekea.selftest.noteText
import app.notekea.selftest.openNote
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.relaunch
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val TASKS = "Tasks and callouts"
private const val TASKS_TEXT = "Buy corn flour"
private const val OUTLINE_LIST = "Headings of this note"

/** The cases of 1.14 about the side panel, the status bar and the zoom (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.workspaceLookCases() {
    noteInfo()
    chatPanel()
    statusBar()
    zoom()
    workspaceDetails()
    workspaceLookManualCases()
}

private fun SuiteBuilder.noteInfo() {
    automated(id = "1.14-08", title = "Note info counts the note, and the panel's folds are kept") {
        openPlaygroundNote(name = TASKS, shownText = TASKS_TEXT)
        listOf("Words", "236", "Reading time", "2 min", "Links", "Tags", "4 of 9", "Created", "Modified")
            .forEach { expectText(text = it) }
        firstTask(state = "not checked").performClick()
        expectText(text = "5 of 9")
        expectFile(path = "$PLAYGROUND/Editor/$TASKS.md", containing = "- [x] Buy corn flour\n")
        click(text = "OUTLINE")
        click(text = "NOTE INFO")
        expectNoText(text = "Reading time")
        relaunch()
        expectText(text = "NOTE INFO")
        expectNoText(text = "Reading time")
        pressInWindow(shortcut = Mod + Shift + Key.O)
        expectLabel(label = OUTLINE_LIST)
    }
}

private fun SuiteBuilder.chatPanel() {
    automated(id = "1.14-09", title = "Chat shows what is coming, and the panel remembers it") {
        openPlaygroundNote(name = WELCOME, shownText = "This is a sample vault")
        click(text = "Chat")
        listOf("Chat with your notes", "COMING SOON", "Your questions will go here").forEach { expectText(text = it) }
        relaunch()
        expectText(text = "Chat with your notes")
        pressInWindow(shortcut = Mod + Shift + Key.O)
        expectLabel(label = OUTLINE_LIST)
        expectNoText(text = "Chat with your notes")
    }
}

private fun SuiteBuilder.statusBar() {
    automated(id = "1.14-12", title = "The status bar shows where the note is, the cursor and the file") {
        openPlaygroundNote(name = TASKS, shownText = TASKS_TEXT)
        val field = noteText(part = TASKS_TEXT)
        val text = field.fetchSemanticsNode().config[SemanticsProperties.EditableText].text
        field.performClick()
        field.performTextInputSelection(selection = TextRange(index = text.indexOf(TASKS_TEXT)))
        listOf("Editor", TASKS, "8:7", "LF", "UTF-8", "236 words").forEach { expectText(text = it) }
        openNote(name = "Windows line endings", shownText = "Every line ends with CR LF.")
        expectText(text = "CRLF")
        expectCursorShown(isShown = true)
        pressInWindow(shortcut = Mod + Key.E)
        expectCursorShown(isShown = false)
        clickInTree(text = "Recipes")
        expectText(text = "Arepas")
        expectNoText(text = "CRLF")
        expectNoText(text = "UTF-8")
    }
}

private fun SuiteBuilder.zoom() {
    automated(id = "1.14-14", title = "Mod+= and Mod+- zoom the whole window, kept until Mod+0") {
        openPlaygroundNote(name = WELCOME, shownText = "This is a sample vault")
        repeat(times = 3) { pressInWindow(shortcut = Mod + Key.Equals) }
        pressInWindow(shortcut = Mod + Key.Minus)
        expectText(text = "120 %")
        relaunch()
        expectText(text = "120 %")
        pressInWindow(shortcut = Mod + Key.Zero)
        expectNoText(text = "120 %")
        clickIcon(label = "About Notekea")
        listOf("Zoom in", "Zoom out", "Actual size").forEach { expectText(text = it) }
    }
}

private fun SuiteBuilder.workspaceDetails() {
    automated(id = "1.14-15", title = "The status bar's path and zoom act, and an empty editor offers what to do") {
        openPlaygroundNote(name = "Formatting practice", shownText = "Weekend arepas")
        click(text = "Editor")
        expectText(text = "Broken properties")
        pressInWindow(shortcut = Mod + Key.Backslash)
        expectNoText(text = "FILES")
        pressInWindow(shortcut = Mod + Key.Equals)
        click(text = "110 %")
        expectNoText(text = "110 %")
        pressInWindow(shortcut = Mod + Key.W)
        listOf("Find a note", "New note", "Today’s note", "Search in notes").forEach { expectText(text = it) }
        click(text = "Find a note")
        expectText(text = "Find a note by name or alias")
    }
}

private fun CaseScope.firstTask(state: String) = ui.onAllNodes(
    matcher = hasContentDescription(value = "Task") and hasStateDescription(value = state)
)[0]

/** Fails unless the status bar shows the cursor's line:column, or no longer does, as [isShown] says. */
private fun CaseScope.expectCursorShown(isShown: Boolean) {
    val cursor = SemanticsMatcher(description = "a line:column") { node ->
        node.config.getOrElseNullable(SemanticsProperties.Text) { null }?.any { CURSOR.matches(it.text) } == true
    }
    val holds = runCatching {
        ui.waitUntil(timeoutMillis = 10_000) {
            ui.onAllNodes(matcher = cursor).fetchSemanticsNodes().isNotEmpty() ==
                isShown
        }
    }.isSuccess
    if (!holds) {
        throw CaseFailure(
            message = "expected the status bar ${if (isShown) "to show" else "not to show"} the cursor"
        )
    }
}

private val CURSOR = Regex(pattern = "\\d+:\\d+")

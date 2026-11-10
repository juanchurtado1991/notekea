package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import app.notekea.autotest.clickInTree
import app.notekea.autotest.noteText
import app.notekea.autotest.openNote
import app.notekea.autotest.pressInWindow
import app.notekea.autotest.relaunch
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val TASKS = "Tasks and callouts"
private const val TASKS_TEXT = "Buy corn flour"
private const val OUTLINE_LIST = "Headings of this note"
private const val NOTE_INFO = "NOTE INFO"
private const val WELCOME_TEXT = "This is a sample vault"

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
    automated(
        id = "1.14-08",
        title = "Note info counts the note, and the panel's folds are kept"
    ) {
        openPlaygroundNote(
            name = TASKS,
            shownText = TASKS_TEXT
        )

        listOf("Words", "236", "Reading time", "2 min", "Links", "Tags", "4 of 9", "Created", "Modified")
            .forEach { expectText(text = it) }

        firstTask(state = "not checked").clickWhenStill()
        expectText(text = "5 of 9")

        expectFile(
            path = "$PLAYGROUND/Editor/$TASKS.md",
            containing = "- [x] Buy corn flour\n"
        )

        click(text = "OUTLINE")
        click(text = NOTE_INFO)
        expectNoText(text = "Reading time")
        relaunch()
        expectText(text = NOTE_INFO)
        expectNoText(text = "Reading time")
        pressInWindow(shortcut = Mod + Shift + Key.O)
        expectLabel(label = OUTLINE_LIST)
    }
}

private fun SuiteBuilder.chatPanel() {
    automated(
        id = "1.14-09",
        title = "Chat takes the panel's place, and the panel remembers it"
    ) {
        openPlaygroundNote(
            name = WELCOME,
            shownText = WELCOME_TEXT
        )

        expectText(text = NOTE_INFO)
        click(text = "Chat")
        expectNoText(text = NOTE_INFO)
        relaunch()

        // The note shows once the window is back, and the panel with it.
        expectText(
            text = WELCOME_TEXT,
            substring = true
        )

        expectNoText(text = NOTE_INFO)
        pressInWindow(shortcut = Mod + Shift + Key.O)
        expectLabel(label = OUTLINE_LIST)
        expectText(text = NOTE_INFO)
    }
}

private fun SuiteBuilder.statusBar() {
    automated(
        id = "1.14-12",
        title = "The status bar shows where the note is, the cursor and the file"
    ) {
        openPlaygroundNote(
            name = TASKS,
            shownText = TASKS_TEXT
        )

        val field = noteText(part = TASKS_TEXT)

        val text = field
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text

        field.clickWhenStill()
        field.performTextInputSelection(selection = TextRange(index = text.indexOf(TASKS_TEXT)))

        listOf("Editor", TASKS, "8:7", "LF", "UTF-8", "236 words").forEach { expectText(text = it) }

        openNote(
            name = "Windows line endings",
            shownText = "Every line ends with CR LF."
        )

        expectText(text = "CRLF")
        expectCursorShown()
        clickInTree(text = "Recipes")
        expectText(text = "Arepas")
        expectNoText(text = "CRLF")
        expectNoText(text = "UTF-8")
    }
}

private fun SuiteBuilder.zoom() {
    automated(
        id = "1.14-14",
        title = "Mod+= and Mod+- zoom the whole window, kept until Mod+0"
    ) {
        openPlaygroundNote(
            name = WELCOME,
            shownText = "This is a sample vault"
        )

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
    automated(
        id = "1.14-15",
        title = "The status bar's path and zoom act, and an empty editor offers what to do"
    ) {
        openPlaygroundNote(
            name = "Formatting practice",
            shownText = "Weekend arepas"
        )

        click(text = "Editor")
        expectText(text = "Broken properties")
        pressInWindow(shortcut = Mod + Key.Backslash)
        expectNoText(text = "FILES")
        pressInWindow(shortcut = Mod + Key.Equals)
        click(text = "110 %")
        expectNoText(text = "110 %")
        pressInWindow(shortcut = Mod + Key.W)

        listOf("Find a note", "New note", "Today’s note", "Search in notes").forEach {
            expectText(text = it)
        }

        click(text = "Find a note")
        expectText(text = "Find a note by name or alias")
    }
}

private fun CaseScope.firstTask(state: String) = ui.onAllNodes(
    matcher = hasContentDescription(value = "Task") and hasStateDescription(value = state)
)[0]

/** Fails unless the status bar shows the cursor's line:column. */
private fun CaseScope.expectCursorShown() {
    val cursor = SemanticsMatcher(description = "a line:column") { node ->
        node
            .config
            .getOrElseNullable(SemanticsProperties.Text) { null }
            ?.any { CURSOR.matches(it.text) } == true
    }

    val holds = runCatching {
        ui
            .waitUntil(timeoutMillis = 10_000) {
                ui
                    .onAllNodes(matcher = cursor)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
    }.isSuccess

    if (!holds) throw CaseFailure(message = "expected the status bar to show the cursor")
}

private val CURSOR = Regex(pattern = "\\d+:\\d+")

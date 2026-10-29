package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.performClick
import app.notekea.selftest.pressInWindow
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val TASKS_NOTE = "Tasks and callouts"
private const val TASKS_FILE = "$PLAYGROUND/Editor/Tasks and callouts.md"
private const val SHOWN = "Buy corn flour"
private const val OPEN_TASKS =
    "1. Open Notekea and open the Playground Vault from the Test data folder (Open folder…).\n" +
        "2. Open the note Editor › Tasks and callouts and click Preview under its title."

/** The cases of 1.7, tasks and callouts (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.taskCases() {
    tasksShowAsCheckboxes()
    checkboxClick()
    toggleWithShortcut()
    calloutColors()
    calloutFolding()
    nestedCallouts()
}

private fun SuiteBuilder.tasksShowAsCheckboxes() {
    automated(id = "1.7-01", title = "Tasks show as checkboxes, the done ones checked") {
        openPlaygroundNote(name = TASKS_NOTE, shownText = SHOWN)
        ui.waitUntil(timeoutMillis = 10_000) { taskBoxes().size == 9 }
        val checked = taskBoxes().count { it.config.getOrNull(SemanticsProperties.StateDescription) == "checked" }
        if (checked != 4) throw CaseFailure(message = "expected 4 of the 9 tasks checked, $checked are")
        pressInWindow(shortcut = Mod + Key.E)
        expectNoText(text = "- [x] Soak the beans", substring = true)
        expectText(text = "Soak the beans", substring = true)
    }
}

private fun SuiteBuilder.checkboxClick() {
    automated(id = "1.7-02", title = "Clicking a task's checkbox checks it in the file") {
        openPlaygroundNote(name = TASKS_NOTE, shownText = SHOWN)
        clickFirstTaskBox()
        expectFile(path = TASKS_FILE, containing = "- [x] Buy corn flour")
        clickFirstTaskBox()
        expectFile(path = TASKS_FILE, containing = "- [ ] Buy corn flour")
    }
}

private fun SuiteBuilder.toggleWithShortcut() {
    automated(id = "1.7-03", title = "Mod+Enter checks the task on the cursor's line, and nothing else") {
        openPlaygroundNote(name = TASKS_NOTE, shownText = SHOWN)
        clickWords(words = "Arepas on Monday")
        press(shortcut = Mod + Key.Enter)
        expectFile(path = TASKS_FILE, containing = "- [x] Arepas on Monday")
        press(shortcut = Mod + Key.Enter)
        expectFile(path = TASKS_FILE, containing = "- [ ] Arepas on Monday")
        val before = Files.readString(file(path = TASKS_FILE))
        clickWords(words = "## Tasks")
        press(shortcut = Mod + Key.Enter)
        Thread.sleep(SAVE_PAUSE_MILLIS)
        if (Files.readString(file(path = TASKS_FILE)) !=
            before
        ) {
            throw CaseFailure(message = "expected the heading to stay as it was")
        }
    }
}

private fun SuiteBuilder.calloutColors() {
    manual(id = "1.7-04", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "Callouts look like cards, each in its color")
        steps(text = "$OPEN_TASKS\n3. Scroll through the Callouts and Aliases sections.")
        expected(
            text = "Each callout is a card with an icon and a title. Note, Abstract, Info, To do, Tip, Success, " +
                "Question and Example are blue; Warning and \"Caution is a warning\" orange; Failure, Danger and Bug " +
                "red; Quote gray. \"TL;DR is an abstract\" looks like Abstract and \"An unknown type is a note\" " +
                "like " +
                "Note. No [! text shows anywhere."
        )
    }
}

private fun SuiteBuilder.calloutFolding() {
    automated(id = "1.7-05", title = "A folded callout opens and folds with a click on its title") {
        openPlaygroundNote(name = TASKS_NOTE, shownText = SHOWN)
        pressInWindow(shortcut = Mod + Key.E)
        expectNoText(text = "This text is hidden until the title is clicked.")
        clickWords(words = "A folded question")
        expectText(text = "This text is hidden until the title is clicked.")
        clickWords(words = "A folded question")
        expectNoText(text = "This text is hidden until the title is clicked.")
        expectText(text = "This text is shown until the title is clicked.")
        clickWords(words = "An open tip")
        expectNoText(text = "This text is shown until the title is clicked.")
    }
}

private fun SuiteBuilder.nestedCallouts() {
    manual(id = "1.7-06", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "Callouts inside callouts, and code inside a callout")
        steps(text = "$OPEN_TASKS\n3. Look at the Nesting section and at the Example callout.")
        expected(
            text = "\"Level one\" holds \"Level two\", which holds \"Level three\", each in its own color. The " +
                "Example callout shows val servings = 6 as colored code, with no > signs. The last quote is a plain " +
                "quote with a bar on its left."
        )
    }
}

private fun CaseScope.taskBoxes() = ui.onAllNodes(matcher = hasContentDescription(value = "Task"))
    .fetchSemanticsNodes()

private fun CaseScope.clickFirstTaskBox() {
    val boxes = ui.onAllNodes(matcher = hasContentDescription(value = "Task"))
    ui.waitUntil(timeoutMillis = 10_000) { boxes.fetchSemanticsNodes().isNotEmpty() }
    val count = boxes.fetchSemanticsNodes().size
    val shown = (0 until count).map { boxes[it] }.filter { it.isDisplayed() }
    shown.minBy { it.fetchSemanticsNode().boundsInRoot.top }.performClick()
}

private const val SAVE_PAUSE_MILLIS = 1_500L

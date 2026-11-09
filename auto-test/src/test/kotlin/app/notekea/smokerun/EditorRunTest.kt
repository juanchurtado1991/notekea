package app.notekea.smokerun

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import kotlin.test.Test

/**
 * Automated smoke runs of the editor (`ROADMAP.md` 1.5, 1.7, 1.9, 1.17): typing saved to the file, a task checked with
 * its shortcut, the outline beside the note and a toolbar format, on a copy of the Sample Vault.
 */
@OptIn(ExperimentalTestApi::class)
class EditorRunTest {
    @Test
    fun `typing in a note is saved to its file once typing pauses`() = smokeRun(name = "editor-typing") { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        openNote(name = PANCAKES, shownText = PANCAKES_TEXT)
        editor(textPart = PANCAKES_TEXT).performTextInputSelection(selection = TextRange(index = 0))
        editor(textPart = PANCAKES_TEXT).performTextInput(text = TYPED)
        waitForFile(app = app, path = PANCAKES_FILE, part = TYPED)
    }

    @Test
    fun `mod+enter checks the task on the cursor line, and the file says so`() = smokeRun(
        name = "editor-tasks"
    ) { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        openNote(name = TASKS, shownText = OPEN_TASK)
        editor(textPart = OPEN_TASK).performTextInputSelection(selection = TextRange(index = TASKS_LINE_OFFSET))
        pressShortcut(key = Key.Enter)
        waitForFile(app = app, path = TASKS_FILE, part = CHECKED_TASK)
    }

    @Test
    fun `the outline lists the note's headings beside it`() = smokeRun(name = "editor-outline") { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        openNote(name = AREPAS, shownText = INGREDIENTS)
        waitForText(text = OUTLINE)
        waitForText(text = STEPS)
    }

    @Test
    fun `bold from the toolbar wraps the selection, saved like typing`() = smokeRun(name = "editor-toolbar") { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        openNote(name = PANCAKES, shownText = PANCAKES_TEXT)
        val start = PANCAKES_BODY.indexOf(string = FLOUR)
        editor(textPart = PANCAKES_TEXT).performTextInputSelection(
            selection = TextRange(start = start, end = start + FLOUR.length)
        )
        onNode(matcher = hasContentDescription(value = BOLD)).performClick()
        waitForFile(app = app, path = PANCAKES_FILE, part = BOLD_FLOUR)
    }

    companion object {
        private const val AREPAS = "Arepas"
        private const val BOLD = "Bold"
        private const val BOLD_FLOUR = "**Flour**"
        private const val CHECKED_TASK = "- [x] An open task"
        private const val FLOUR = "Flour"
        private const val HOME = "Home"
        private const val INGREDIENTS = "Ingredients"
        private const val OPEN_TASK = "- [ ] An open task"
        private const val OUTLINE = "OUTLINE"
        private const val PANCAKES = "Pancakes"
        private const val PANCAKES_BODY = "# Pancakes\n\nFlour, milk, an egg and a pinch of salt."
        private const val PANCAKES_FILE = "Recipes/Pancakes.md"
        private const val PANCAKES_TEXT = "Rest the batter"
        private const val STEPS = "Steps"
        private const val TASKS = "Tasks"
        private const val TASKS_FILE = "Guides/Tasks.md"
        private const val TASKS_LINE_OFFSET = 12
        private const val TYPED = "Smoke run was here. "
    }
}

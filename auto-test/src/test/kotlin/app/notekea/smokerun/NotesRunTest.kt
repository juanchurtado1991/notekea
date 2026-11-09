package app.notekea.smokerun

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import java.io.File
import java.time.LocalDate
import kotlin.test.Test

/**
 * Automated smoke runs of what notes hold and how they are found (`ROADMAP.md` 1.6, 1.8, 1.10, 1.12, 1.14, 1.15):
 * a search inside the notes, attachments, today's daily note, the template picker, properties and embeds, on a copy of
 * the Sample Vault.
 */
@OptIn(ExperimentalTestApi::class)
class NotesRunTest {
    @Test
    fun `a search inside the notes finds the ones that mention a word`() = smokeRun(name = "search") { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        pressShortcut(key = Key.F, withShift = true)
        waitForText(text = SEARCH_PLACEHOLDER)
        onNode(matcher = hasSetTextAction() and hasText(text = SEARCH_PLACEHOLDER)).performTextInput(text = TOMATOES)
        waitForText(text = GARDEN_PLAN)
        waitForText(text = TOMATO_SOUP)
    }

    @Test
    fun `pictures, files and a missing file show in the note`() = smokeRun(name = "attachments") { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        openNote(name = IMAGES_AND_FILES, shownText = MENU_PDF)
        waitForText(text = MISSING_FILE)
    }

    @Test
    fun `today makes today's daily note in the vault's daily notes folder`() = smokeRun(name = "daily-notes") { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        onNode(matcher = hasContentDescription(value = TODAY)).performClick()
        waitUntil(timeoutMillis = WAIT_MILLIS) {
            File(app.vaultPath(name = SAMPLE_VAULT), DAILY_FOLDER + LocalDate.now() + MARKDOWN).exists()
        }
    }

    @Test
    fun `the template picker lists the vault's templates`() = smokeRun(name = "templates") { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        openNote(name = PANCAKES, shownText = PANCAKES_TEXT)
        pressShortcut(key = Key.T)
        waitForText(text = TEMPLATE_PLACEHOLDER)
        waitForText(text = MEETING)
    }

    @Test
    fun `properties of every type show under the title`() = smokeRun(name = "properties") { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        openNote(name = PROPERTIES, shownText = PROPERTIES_TEXT)
        listOf(STATUS, COUNT, DUE).forEach { waitForText(text = it) }
    }

    @Test
    fun `embeds nest, stop where they come back around and say when a note is missing`() =
        smokeRun(name = "embeds") { app ->
            openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
            openNote(name = EMBEDS, shownText = SECTION)
            waitForText(text = CIRCULAR)
            waitForText(text = MISSING_NOTE)
        }

    companion object {
        private const val CIRCULAR = "Circular embed: this note is already shown above, so it stops here."
        private const val COUNT = "count"
        private const val DAILY_FOLDER = "Journal/Daily/"
        private const val DUE = "due"
        private const val EMBEDS = "Embeds"
        private const val GARDEN_PLAN = "Garden plan"
        private const val HOME = "Home"
        private const val IMAGES_AND_FILES = "Images and files"
        private const val MARKDOWN = ".md"
        private const val MEETING = "Meeting"
        private const val MENU_PDF = "![[Weekly menu.pdf]]"
        private const val MISSING_FILE = "This file is missing"
        private const val MISSING_NOTE = "This note does not exist yet."
        private const val PANCAKES = "Pancakes"
        private const val PANCAKES_TEXT = "Rest the batter"
        private const val PROPERTIES = "Properties"
        private const val PROPERTIES_TEXT = "Text, number, checkbox"
        private const val SEARCH_PLACEHOLDER = "Search the text of your notes"
        private const val SECTION = "A section of a note"
        private const val STATUS = "status"
        private const val TEMPLATE_PLACEHOLDER = "Find a template to insert"
        private const val TODAY = "Today"
        private const val TOMATO_SOUP = "Tomato soup"
        private const val TOMATOES = "tomatoes"
        private const val WAIT_MILLIS = 10_000L
    }
}

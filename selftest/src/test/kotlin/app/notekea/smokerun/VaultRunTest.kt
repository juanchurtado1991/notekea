package app.notekea.smokerun

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange

/**
 * Automated smoke runs of the vault (`ROADMAP.md` 1.3, 1.4, 1.11, 1.13): opening it and coming back to it from the
 * recent vaults, leaving it right after typing, its tree, tabs and bookmarks, on a copy of the Sample Vault.
 */
@OptIn(ExperimentalTestApi::class)
class VaultRunTest {
    @Test
    fun `a vault opens, is left for the welcome screen and opens again from Recent`() = smokeRun(
        name = "vault"
    ) { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        onNode(matcher = hasContentDescription(value = SWITCH_VAULT)).performClick()
        waitForText(text = RECENT)
        onNode(matcher = hasText(text = SAMPLE_VAULT)).performClick()
        waitForText(text = HOME)
    }

    @Test
    fun `leaving the vault right after typing keeps what was typed in the file`() = smokeRun(
        name = "vault-leave"
    ) { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        openNote(name = PANCAKES, shownText = PANCAKES_TEXT)
        editor(textPart = PANCAKES_TEXT).performTextInputSelection(selection = TextRange(index = 0))
        editor(textPart = PANCAKES_TEXT).performTextInput(text = TYPED)
        onNode(matcher = hasContentDescription(value = SWITCH_VAULT)).performClick()
        waitForText(text = RECENT)

        // Checked at once, not after the autosave's pause: leaving the vault clears its editors, so it saves first.
        assertTrue(actual = File(app.vaultPath(name = SAMPLE_VAULT), PANCAKES_FILE).readText().contains(other = TYPED))
    }

    @Test
    fun `a folder opens in the tree, and notes open in tabs that close`() = smokeRun(name = "tree-and-tabs") { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        onNode(matcher = hasText(text = RECIPES)).performClick()
        waitForText(text = PANCAKES)
        openNote(name = AREPAS, shownText = INGREDIENTS)
        openNote(name = PANCAKES, shownText = PANCAKES_TEXT)
        val before = onAllNodes(matcher = hasContentDescription(value = CLOSE_TAB)).fetchSemanticsNodes().size
        pressShortcut(key = Key.W)
        waitForIdle()
        val after = onAllNodes(matcher = hasContentDescription(value = CLOSE_TAB)).fetchSemanticsNodes().size

        assertEquals(expected = before - 1, actual = after)
    }

    @Test
    fun `the open note is bookmarked with its shortcut`() = smokeRun(name = "bookmarks") { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        openNote(name = PANCAKES, shownText = PANCAKES_TEXT)
        pressShortcut(key = Key.B, withShift = true)
        waitForText(text = BOOKMARKS)
    }

    companion object {
        private const val AREPAS = "Arepas"
        private const val BOOKMARKS = "BOOKMARKS"
        private const val CLOSE_TAB = "Close tab"
        private const val HOME = "Home"
        private const val INGREDIENTS = "Ingredients"
        private const val PANCAKES = "Pancakes"
        private const val PANCAKES_FILE = "Recipes/Pancakes.md"
        private const val PANCAKES_TEXT = "Rest the batter"
        private const val RECENT = "Recent"
        private const val RECIPES = "Recipes"
        private const val SWITCH_VAULT = "Open another vault"
        private const val TYPED = "Typed before leaving. "
    }
}

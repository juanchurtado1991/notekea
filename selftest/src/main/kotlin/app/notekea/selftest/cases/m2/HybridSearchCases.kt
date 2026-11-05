package app.notekea.selftest.cases.m2

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isFocused
import app.notekea.selftest.pressInWindow
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import app.notekea.selftest.openVault
import app.notekea.selftest.waitForResult
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val HOME = "Home"
private const val REST_LINE = "Mix the flour, the water and the salt, then let it rest five minutes. ^dough"
private const val HOT_PAN_LINE = "Shape discs and cook them on a hot pan, five minutes a side."
private const val OPEN_SEARCH =
    "1. In Notekea, open the Sample Vault (see TEST DATA above) and wait until the bar at the bottom says " +
        "\"40 notes ready to search\".\n2. Press Ctrl+Shift+F (Cmd+Shift+F on macOS)."

/** The cases of 2.6, hybrid search (`MVP_SMOKE_TEST.md`): the palette's Content tab by meaning and keywords. */
fun SuiteBuilder.hybridSearchCases() {
    passageRows()
    byMeaning()
    nothingClose()
    exactWordsNotice()
}

private fun SuiteBuilder.passageRows() {
    automated(id = "2.6-01", title = "A search result names its heading and opens the note at the passage") {
        openVault(name = SAMPLE, shownText = HOME)
        pressInWindow(shortcut = Mod + Shift + Key.F)
        type(text = "rest five minutes")
        waitForResult(name = "Arepas")
        expectText(text = "Recipes › Steps", substring = true)
        press(key = Key.Enter)
        expectPassageSelected()
        pressInWindow(shortcut = Mod + Shift + Key.F)
        type(text = "cook them on a hot pan")
        waitForResult(name = "Arepas")
        press(key = Key.Enter)
        expectNoText(text = "Search the text of your notes")
        expectLineSelected(line = HOT_PAN_LINE)
        pressInWindow(shortcut = Mod + Key.E)
        expectNoText(text = HOT_PAN_LINE)
        pressInWindow(shortcut = Mod + Shift + Key.F)
        type(text = "rest five minutes")
        waitForResult(name = "Arepas")
        press(key = Key.Enter)
        expectPassageSelected()
    }
}

private fun CaseScope.expectPassageSelected() = expectLineSelected(line = REST_LINE)

/** The text field holds the keyboard with exactly [line] selected. */
private fun CaseScope.expectLineSelected(
    line: String
) {
    val field = isFocused() and hasSetTextAction()
    ui.waitUntil(timeoutMillis = 10_000) {
        ui.onAllNodes(matcher = field).fetchSemanticsNodes().any { node ->
            val text = node.config[SemanticsProperties.EditableText].text
            val selection = node.config[SemanticsProperties.TextSelectionRange]
            text.substring(selection.min, selection.max) == line
        }
    }
}

private fun SuiteBuilder.byMeaning() {
    manual(id = "2.6-02", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "A search by meaning finds a note without its words, in another language too")
        steps(text = "$OPEN_SEARCH\n3. Type: cómo hacer la masa\n4. Clear it and type: trip to Portugal")
        expected(
            text = "The first search lists Arepas near the top with a line about its dough, although the note " +
                "has none of those words. The second lists Trip to Lisbon first."
        )
    }
}

private fun SuiteBuilder.nothingClose() {
    manual(id = "2.6-03", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "A search with nothing close finds nothing, instead of the closest notes")
        steps(text = "$OPEN_SEARCH\n3. Type: football match results")
        expected(text = "The palette says \"No notes match\".")
    }
}

private fun SuiteBuilder.exactWordsNotice() {
    manual(id = "2.6-04", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "While the index is not ready, search keeps working and says so")
        steps(
            text = "$OPEN_SEARCH\n3. Press Esc, click the index status in the bar at the bottom and choose " +
                "Rebuild index.\n4. While it runs, press Ctrl+Shift+F (Cmd+Shift+F on macOS) and type: tomato"
        )
        expected(
            text = "Notes with the word tomato are listed, with \"Showing exact words only until your notes are " +
                "ready to search.\" below them. Once the bar says the notes are ready, the line is gone."
        )
    }
}

@file:OptIn(ExperimentalTestApi::class)

package app.notekea.smokerun

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput

private const val CLOSE_TAB = "Close tab"
private const val MAX_TABS = 100
private const val MILLIS = 1_000L
private const val MINUTE = 60L
private const val RECENT = "Recent"
private const val SCROLL_STEPS = 40
private const val SEARCH_MILLIS = 60_000L
private const val SEARCH_PLACEHOLDER = "Search the text of your notes"
private const val SCROLL_TICKS = 25f
private const val SWITCH_VAULT = "Open another vault"

/**
 * The steps of a memory run (`ROADMAP.md` 1.18), each followed by a sample of [run]'s memory named after it: open the
 * [notes] (name to a text it shows) in tabs and close them, search the notes' text for [search] (a word to a result it
 * shows), scroll [longNote] to its end in Edit and Preview, stay idle, then leave the vault and open it again.
 */
internal fun ComposeUiTest.measureVault(
    app: SmokeRunApp,
    run: String,
    vault: String,
    notes: List<Pair<String, String>>,
    search: Pair<String, String>,
    openMillis: Long,
    longNote: Pair<String, String>?
) {
    SmokeRunReport.sample(run = run, moment = "opened")
    notes.forEach { (name, text) -> openNote(name = name, shownText = text, timeoutMillis = openMillis) }
    SmokeRunReport.sample(run = run, moment = "${notes.size}-notes-open")
    closeAllTabs(app = app)
    SmokeRunReport.sample(run = run, moment = "notes-closed")
    searchText(app = app, word = search.first, result = search.second)
    SmokeRunReport.sample(run = run, moment = "text-searched")
    longNote?.let { (name, text) ->
        scrollLongNote(run = run, name = name, end = text)
        closeAllTabs(app = app)
    }
    idle(run = run)
    onNode(matcher = hasContentDescription(value = SWITCH_VAULT)).performClick()
    waitForText(text = RECENT)
    SmokeRunReport.sample(run = run, moment = "vault-closed")
    onNode(matcher = hasText(text = vault)).performClick()
    waitForIdle()
    waitUntil(timeoutMillis = openMillis) {
        onAllNodes(matcher = hasContentDescription(value = SWITCH_VAULT)).fetchSemanticsNodes().isNotEmpty()
    }
    SmokeRunReport.sample(run = run, moment = "reopened")
}

/** Gives the focus back to the window once nothing holds it, as a window keeps getting keys in the app. */
private fun ComposeUiTest.focusWindow(
    app: SmokeRunApp
) = runOnIdle { app.windowFocus.requestFocus() }

/** Searches the text of the notes for [word] (`Mod+Shift+F`) until [result] shows, then closes the search. */
private fun ComposeUiTest.searchText(
    app: SmokeRunApp,
    word: String,
    result: String
) {
    pressShortcut(key = Key.F, withShift = true)
    waitForText(text = SEARCH_PLACEHOLDER)
    val field = onNode(matcher = hasSetTextAction() and hasText(text = SEARCH_PLACEHOLDER))
    field.performTextInput(text = word)
    waitForText(text = result, substring = true, timeoutMillis = SEARCH_MILLIS)
    onNode(matcher = hasSetTextAction() and hasText(text = word)).performKeyInput { pressKey(key = Key.Escape) }
    waitForIdle()
    focusWindow(app = app)
}

/** Opens [name], scrolls it to [end] in Edit, then in Preview (`Mod+E`), with a sample after each. */
private fun ComposeUiTest.scrollLongNote(
    run: String,
    name: String,
    end: String
) {
    openNote(name = name, shownText = name)
    scrollDown()
    SmokeRunReport.sample(run = run, moment = "long-note-edit")
    pressShortcut(key = Key.E)
    waitForIdle()
    scrollDown()
    waitForText(text = end, substring = true)
    SmokeRunReport.sample(run = run, moment = "long-note-preview")
}

/** Turns the mouse wheel over the middle of the window, where the note is, down to its end. */
private fun ComposeUiTest.scrollDown() {
    repeat(times = SCROLL_STEPS) {
        window().performMouseInput {
            moveTo(position = Offset(x = width * 0.6f, y = height * 0.5f))
            scroll(delta = SCROLL_TICKS)
        }
        waitForIdle()
    }
}

/** Closes every tab with its close button, then gives the focus back to the window; fails if a tab stays open. */
internal fun ComposeUiTest.closeAllTabs(
    app: SmokeRunApp
) {
    val closeButtons = { onAllNodes(matcher = hasContentDescription(value = CLOSE_TAB)) }
    repeat(times = MAX_TABS) {
        if (closeButtons().fetchSemanticsNodes().isEmpty()) {
            focusWindow(app = app)
            return
        }
        closeButtons().onFirst().performClick()
        waitForIdle()
    }
    error("Tabs still open after closing $MAX_TABS")
}

/** With the `smokeRun` task's idle time, waits one minute and then the rest, with a sample after each. */
private fun idle(
    run: String
) {
    val idle = SmokeRunReport.idleSeconds
    if (idle <= 0) return
    Thread.sleep(minOf(idle, MINUTE) * MILLIS)
    SmokeRunReport.sample(run = run, moment = "idle-1min")
    if (idle > MINUTE) {
        Thread.sleep((idle - MINUTE) * MILLIS)
        SmokeRunReport.sample(run = run, moment = "idle-${idle}s")
    }
}

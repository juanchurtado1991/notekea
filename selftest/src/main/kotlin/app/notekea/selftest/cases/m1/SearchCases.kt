package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.performClick
import app.notekea.selftest.SmokeScenarios
import app.notekea.selftest.expectOrderOnScreen
import app.notekea.selftest.openNote
import app.notekea.selftest.openVault
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.noteText
import app.notekea.selftest.waitForResult
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val FIND_NOTE = "Find a note by name or alias"
private const val SEARCH_TEXT = "Search the text of your notes"

/** The cases of 1.6 that search inside the notes and drive the palette (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.searchCases() {
    contentSearch()
    tagSearch()
    paletteKeyboard()
    tagLink()
    emptyResults()
    outsideEditFound()
    largeVaultSearch()
    typingAfterThePalette()
    railSearchButton()
}

private fun SuiteBuilder.contentSearch() {
    automated(id = "1.6-04", title = "Searching the notes' text lists matches as plain lines") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        pressInWindow(shortcut = Mod + Shift + Key.F)
        expectText(text = SEARCH_TEXT)
        type(text = "tomato")
        expectText(text = "Planted the last tomato seedlings", substring = true)
        expectOrderOnScreen(texts = listOf("2026-10-20", "Pasta e fagioli", "Tomato plan"))
        expectText(text = "notes in Tomato plan", substring = true)
        expectNoText(text = "[[Tomato plan", substring = true)
    }
}

private fun SuiteBuilder.tagSearch() {
    automated(id = "1.6-05", title = "Searching a tag finds the notes that have it") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        pressInWindow(shortcut = Mod + Shift + Key.F)
        type(text = "#recipe")
        listOf("Arepas", "Café con leche", "Pasta e fagioli").forEach { waitForResult(name = it) }
        expectText(text = "#recipe/drinks", substring = true)
        expectNoText(text = "Tomato plan")
    }
}

private fun SuiteBuilder.paletteKeyboard() {
    automated(id = "1.6-06", title = "Tab switches the palette's search, Esc closes it") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        pressInWindow(shortcut = Mod + Key.P)
        type(text = "a")
        press(key = Key.Tab)
        expectText(text = "a")
        press(key = Key.Tab)
        expectText(text = "a")
        repeat(times = 3) { press(key = Key.DirectionDown) }
        repeat(times = 4) { press(key = Key.DirectionUp) }
        press(key = Key.Escape)
        expectNoText(text = "a")
        expectText(text = WELCOME)
    }
}

private fun SuiteBuilder.tagLink() {
    automated(id = "1.6-07", title = "A tag in Preview opens the search for it") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        openNote(name = "2026-10-21", shownText = "#travel")
        pressInWindow(shortcut = Mod + Key.E)
        expectText(text = "#travel", substring = true)
        clickWords(words = "#travel")
        waitForResult(name = "Lisbon trip")
        expectText(text = "#journal #travel", substring = true)
    }
}

private fun SuiteBuilder.emptyResults() {
    automated(id = "1.6-08", title = "No match, and an empty Content search, explain themselves") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        pressInWindow(shortcut = Mod + Key.P)
        type(text = "zzz")
        expectText(text = "No notes match")
        expectText(text = "Try fewer letters, or press Tab to search inside notes.")
        press(key = Key.Tab)
        clearText()
        expectText(text = "Search inside your notes")
        expectText(text = "Type words or a #tag.")
    }
}

private fun SuiteBuilder.outsideEditFound() {
    automated(id = "1.6-09", title = "A note changed by another app is found by its new text") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        SmokeScenarios(smokeData = folders.data, scratch = folders.data).editNote(time = "10:00:00")
        pressInWindow(shortcut = Mod + Shift + Key.F)
        type(text = "edited outside")
        waitForResult(name = "Shared note")
        expectText(text = "Edited outside Notekea at 10:00:00", substring = true)
    }
}

private fun SuiteBuilder.largeVaultSearch() {
    automated(id = "1.6-10", title = "Searching the large vault by name and by text") {
        openVault(name = "Ghost Serializer", shownText = "ghost-compiler")
        pressInWindow(shortcut = Mod + Key.P)
        type(text = "flatreader")
        waitForResult(name = "GhostJsonFlatReader")
        press(key = Key.Escape)
        pressInWindow(shortcut = Mod + Shift + Key.F)
        type(text = "perfect hash")
        waitForResult(name = "PerfectHashFinder")
    }
}

private fun SuiteBuilder.typingAfterThePalette() {
    automated(id = "1.6-11", title = "After the palette, typing goes on in the note") {
        openPlaygroundNote(name = "Arepas", shownText = AREPAS_FLOUR)
        noteText(part = AREPAS_FLOUR).performClick()
        press(shortcut = Mod + Key.P)
        press(key = Key.Escape)
        type(text = "First")
        press(shortcut = Mod + Key.P)
        type(text = "pasta")
        waitForResult(name = "Pasta e fagioli")
        press(key = Key.Enter)
        expectText(text = "Pasta e fagioli")
        type(text = "Second")
        expectFile(path = "$PLAYGROUND/Recipes/Arepas.md", containing = "First")
        expectFile(path = "$PLAYGROUND/Recipes/Pasta e fagioli.md", containing = "Second")
    }
}

private fun SuiteBuilder.railSearchButton() {
    automated(id = "1.6-12", title = "The tool rail's magnifier opens the palette") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        openPaletteFromRail()
        press(shortcut = Mod + Key.Backslash)
        openPaletteFromRail()
    }
}

private fun CaseScope.openPaletteFromRail() {
    clickIcon(label = "Search notes")
    expectText(text = FIND_NOTE)
    press(key = Key.Escape)
    expectNoText(text = FIND_NOTE)
}

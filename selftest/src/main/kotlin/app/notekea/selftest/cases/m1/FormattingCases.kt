package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.performClick
import app.notekea.selftest.expectOrderOnScreen
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.waitForResult
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val THREE_LINES = "Buy corn flour\nBuy fresh cheese\nSoak the beans"

/** The cases of 1.17 about the toolbar's buttons and the shortcuts of formatting (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.formattingCases() {
    toolbarButtons()
    headingAndEmphasis()
    listButtons()
    noteLink()
    webLinkAndTags()
    formattingMoreCases()
}

private fun SuiteBuilder.toolbarButtons() {
    automated(id = "1.17-01", title = "The toolbar's buttons and More hold every format", window = WIDE_WINDOW) {
        openFormatting()
        listOf("Heading", "Bold", "Italic", "Link to a note", "Tag", "Checklist", "Bulleted list", "Insert template")
            .forEach { expectLabel(label = it) }
        expectLabel(label = "Find in note")
        clickIcon(label = "More")
        listOf(
            "Web link", "Numbered list", "Callout", "Code block", "Image or file", "Add property", "Quote", "Divider",
            "Table"
        ).forEach { expectText(text = it) }
        press(key = Key.Escape)
        click(text = "Preview")
        expectNoText(text = "Weekend arepas\n", substring = true)
        ui.waitUntil(timeoutMillis = 10_000) { !hasLabel(label = "Bold") }
        expectLabel(label = "Find in note")
        pressInWindow(shortcut = Mod + Key.E)
        expectLabel(label = "Bold")
    }
}

private fun SuiteBuilder.headingAndEmphasis() {
    automated(id = "1.17-02", title = "Heading, Bold and Italic format the line or the words, and undo themselves") {
        openFormatting()
        heading(level = "Large heading")
        expectInNote(text = "\n# Weekend arepas\n")
        heading(level = "Large heading")
        expectInNote(text = "\nWeekend arepas\n")
        heading(level = "Medium heading")
        expectInNote(text = "\n## Weekend arepas\n")
        select(words = "corn flour")
        clickIcon(label = "Bold")
        expectInNote(text = "Buy **corn flour**")
        clickIcon(label = "Bold")
        expectInNote(text = "Buy corn flour")
        select(words = "fresh")
        press(shortcut = Mod + Key.I)
        expectInNote(text = "Buy *fresh* cheese")
        cursorAt(words = "Soak the bea")
        press(shortcut = Mod + Key.B)
        expectInNote(text = "Soak the **beans**")
        press(shortcut = Mod + Key.B)
        expectInNote(text = "Soak the beans")
        cursorAt(words = "nothing below is Markdown yet.")
        clickIcon(label = "Bold")
        expectSelected(words = "bold text")
        type(text = "soft")
        expectInNote(text = "Markdown yet.**soft**")
        expectFile(path = FORMATTING_FILE, containing = "## Weekend arepas")
    }
}

private fun SuiteBuilder.listButtons() {
    automated(
        id = "1.17-03",
        title = "The list buttons change every selected line and keep them selected",
        window = WIDE_WINDOW
    ) {
        openFormatting()
        select(words = THREE_LINES)
        clickIcon(label = "Checklist")
        expectInNote(text = "- [ ] Buy corn flour\n- [ ] Buy fresh cheese\n- [ ] Soak the beans")
        clickIcon(label = "More")
        click(text = "Numbered list")
        expectInNote(text = "1. Buy corn flour\n2. Buy fresh cheese\n3. Soak the beans")
        clickIcon(label = "Bulleted list")
        expectInNote(text = "- Buy corn flour\n- Buy fresh cheese\n- Soak the beans")
        clickIcon(label = "Checklist")
        expectInNote(text = "- [ ] Buy corn flour\n- [ ] Buy fresh cheese\n- [ ] Soak the beans")
        val tasks = ui.onAllNodes(
            matcher = hasContentDescription(value = "Task") and hasStateDescription("not checked")
        )
        ui.waitUntil(timeoutMillis = 10_000) { tasks.fetchSemanticsNodes().size == 3 }
        tasks.fetchSemanticsNodes().withIndex().maxBy {
            it.value.boundsInRoot.top
        }.index.let { tasks[it].performClick() }
        expectInNote(text = "- [x] Soak the beans")
    }
}

private fun SuiteBuilder.noteLink() {
    automated(id = "1.17-04", title = "Link to a note links the selected words to the note picked") {
        openFormatting()
        select(words = "coffee")
        clickIcon(label = "Link to a note")
        expectText(text = "Choose a note to link")
        type(text = "cafe")
        waitForResult(name = "Café con leche")
        press(key = Key.Enter)
        expectInNote(text = "[[Café con leche|coffee]]")
        press(shortcut = Mod + Key.K)
        expectText(text = "Choose a note to link")
        press(key = Key.Escape)
        expectNoText(text = "Choose a note to link")
        type(text = " hot")
        expectInNote(text = "[[Café con leche|coffee]] hot")
        pressInWindow(shortcut = Mod + Key.E)
        clickWords(words = "coffee")
        tab(name = "Café con leche")
    }
}

private fun SuiteBuilder.webLinkAndTags() {
    automated(id = "1.17-05", title = "Web link and Tag write a link and the vault's tags") {
        openFormatting()
        select(words = "hot pan")
        press(shortcut = Mod + Shift + Key.K)
        expectInNote(text = "[hot pan](https://)")
        expectSelected(words = "https://")
        type(text = "https://example.com")
        expectInNote(text = "[hot pan](https://example.com)")
        cursorAt(words = "Weekend arepas")
        clickIcon(label = "Tag")
        expectText(text = "Find or create a tag")
        expectOrderOnScreen(texts = listOf("#recipe", "#journal", "#smoke", "#travel", "#crlf", "#editor"))
        pickTag(typed = "rec", expected = listOf("#recipe", "#recipe/drinks", "New tag: #rec"))
        expectInNote(text = "Weekend arepas #recipe")
        pickTag(typed = "weekend", expected = listOf("#weekend"))
        expectInNote(text = "Weekend arepas #recipe #weekend")
        expectText(text = "Saved")
        clickIcon(label = "Tag")
        type(text = "week")
        expectText(text = "#weekend")
        press(key = Key.Escape)
        type(text = "now")
        expectInNote(text = "#weekend now")
    }
}

private fun CaseScope.heading(level: String) {
    cursorAt(words = "Weekend are")
    clickIcon(label = "Heading")
    click(text = level)
}

private fun CaseScope.pickTag(
    typed: String,
    expected: List<String>
) {
    if (!isTagPickerOpen()) clickIcon(label = "Tag")
    type(text = typed)
    expected.forEach { expectText(text = it) }
    press(key = Key.Enter)
}

private fun CaseScope.isTagPickerOpen(): Boolean =
    ui.onAllNodes(matcher = androidx.compose.ui.test.hasText(text = "Find or create a tag")).fetchSemanticsNodes()
        .isNotEmpty()

internal fun CaseScope.hasLabel(label: String): Boolean =
    ui.onAllNodes(matcher = hasContentDescription(value = label)).fetchSemanticsNodes().isNotEmpty()

internal fun CaseScope.expectLabel(label: String) {
    ui.waitUntil(timeoutMillis = 10_000) { hasLabel(label = label) }
}

package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.performTextReplacement
import app.notekea.selftest.pressInWindow
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder
import com.testbuddy.WindowSize

private const val PAN_LINE = "Remember that the pan stays hot for a long time."
private const val PLAIN_LINES = "Buy corn flour\nBuy fresh cheese\nSoak the beans"
private const val CHECKLIST = "- [ ] Buy corn flour\n- [ ] Buy fresh cheese\n- [x] Soak the beans"
private const val TIP = "> [!tip] Tip\n> Use a pan"
private const val TYPED_CODE = "val hello = \"World\" -> x // != y"

/** The cases of 1.17 about More's formats, lists and code (`MVP_SMOKE_TEST.md`). */
internal fun SuiteBuilder.formattingMoreCases() {
    callouts()
    tableCodeDividerQuote()
    narrowToolbar()
    undoAndObsidian()
    listEditing()
    codeTyping()
}

private fun SuiteBuilder.callouts() {
    automated(id = "1.17-06", title = "Callout wraps the line or inserts one, folded when asked") {
        openFormatting()
        select(words = PAN_LINE)
        openFromMore(item = "Callout")
        expectText(text = "A colored box for a tip, a warning", substring = true)
        click(text = "Start folded")
        click(text = "Warning")
        expectInNote(text = "> [!warning]- Warning\n> $PAN_LINE")
        cursorAtEnd()
        openFromMore(item = "Callout")
        click(text = "Start folded")
        click(text = "Tip")
        expectInNote(text = "> [!tip] Tip\n> Write here")
        expectSelected(words = "Write here")
        type(text = "Use a cast-iron pan")
        expectInNote(text = "> [!tip] Tip\n> Use a cast-iron pan")
        pressInWindow(shortcut = Mod + Key.E)
        expectNoText(text = PAN_LINE)
        click(text = "Warning")
        expectText(text = PAN_LINE)
    }
}

private fun SuiteBuilder.tableCodeDividerQuote() {
    automated(id = "1.17-07", title = "More inserts a table, a code block, a divider and a quote") {
        openFormatting()
        cursorAtEnd()
        openFromMore(item = "Code block")
        listOf("Plain text", "Python", "JavaScript", "TypeScript", "Java", "C#", "C++", "Go", "Rust", "Shell")
            .forEach { expectText(text = it) }
        click(text = "Kotlin")
        expectInNote(text = "```kotlin\ncode\n```")
        expectSelected(words = "code")
        cursorAtEnd()
        openFromMore(item = "Divider")
        expectInNote(text = "\n---")
        cursorAtEnd()
        openFromMore(item = "Quote")
        expectInNote(text = "\n> Quote")
        pressInWindow(shortcut = Mod + Key.E)
        expectText(text = "Quote")
        expectNoText(text = "```kotlin", substring = true)
    }
}

private fun SuiteBuilder.narrowToolbar() {
    automated(
        id = "1.17-08",
        title = "In a narrow window the buttons that do not fit move to the top of More",
        window = WindowSize(width = 800, height = 800)
    ) {
        openFormatting()
        if (hasLabel(label = "Checklist")) throw CaseFailure(message = "expected Checklist inside More")
        clickIcon(label = "More")
        listOf("Checklist", "Bulleted list", "Insert template", "Web link").forEach { expectText(text = it) }
        select(words = PLAIN_LINES)
        openFromMore(item = "Checklist")
        expectInNote(text = "- [ ] Buy corn flour")
    }
}

private fun SuiteBuilder.undoAndObsidian() {
    manual(id = "1.17-09", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "Undo takes back a toolbar change, and Obsidian shows the same formats")
        steps(
            text =
            "1. In Notekea, open the Playground Vault from the Test data folder (Open folder…, after Open another vault at the bottom left if a vault is open).\n2. Open " +
                "Editor/Formatting practice, select \"corn flour\" and click Bold (B) in the bar under the " +
                "title.\n3. Press Ctrl+Z (Cmd+Z on a Mac), then Ctrl+Shift+Z (Cmd+Shift+Z).\n4. Format the note with " +
                "the bar: a heading, a checklist, a link, a tag, a callout, a table, a code block and a quote. Wait " +
                "for \"Saved\" at the bottom right.\n5. If Obsidian is installed, open the Playground Vault there " +
                "and open the same note."
        )
        expected(
            text = "Ctrl+Z takes the bold away in one step and Ctrl+Shift+Z puts it back. Obsidian shows the same " +
                "heading, bold, checklist, link, tag, callout, table, code block and quote as Notekea's Preview."
        )
    }
}

private fun SuiteBuilder.listEditing() {
    automated(id = "1.17-10", title = "Enter, Tab and Backspace continue, nest and end lists and callouts") {
        openFormatting()
        editor().performTextReplacement(text = editorText().replace(PLAIN_LINES, CHECKLIST) + "\n$TIP")
        cursorAt(words = "- [x] Soak the beans")
        press(key = Key.Enter)
        type(text = "Buy salt")
        expectInNote(text = "- [x] Soak the beans\n- [ ] Buy salt")
        press(key = Key.Enter)
        press(key = Key.Enter)
        expectInNote(text = "- [ ] Buy salt\n\n")
        cursorAt(words = "Buy fresh cheese")
        press(key = Key.Tab)
        expectInNote(text = "- [ ] Buy corn flour\n  - [ ] Buy fresh cheese")
        press(shortcut = Shift + Key.Tab)
        expectInNote(text = "- [ ] Buy corn flour\n- [ ] Buy fresh cheese")
        cursorAt(words = "Buy salt", before = true)
        press(key = Key.Backspace)
        expectInNote(text = "\nBuy salt")
        cursorAt(words = "> Use a pan")
        press(key = Key.Enter)
        expectInNote(text = "> Use a pan\n> ")
    }
}

private fun SuiteBuilder.codeTyping() {
    automated(id = "1.17-11", title = "Typing in a code block keeps every character as typed") {
        openFormatting()
        editor().performTextReplacement(text = editorText() + "\n```kotlin\ncode\n```\n")
        select(words = "code\n```")
        select(words = "code")
        type(text = TYPED_CODE)
        expectInNote(text = "```kotlin\n$TYPED_CODE\n```")
        cursorAt(words = "Weekend arepas")
        type(text = " ->")
        expectInNote(text = "Weekend arepas ->")
        expectFile(path = FORMATTING_FILE, containing = TYPED_CODE)
    }
}

private fun CaseScope.openFromMore(item: String) {
    clickIcon(label = "More")
    click(text = item)
}

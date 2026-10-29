package app.notekea.selftest.cases.m1

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import app.notekea.selftest.noteText
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.WindowSize

internal const val FORMATTING = "Formatting practice"
internal const val FORMATTING_TEXT = "Plain text to format with the toolbar"
internal const val FORMATTING_FILE = "$PLAYGROUND/Editor/Formatting practice.md"

// Wide enough for every button of the toolbar outside More, with the sidebar and the side panel shown.
internal val WIDE_WINDOW = WindowSize(width = 1700, height = 900)

/** Opens Formatting practice of the Playground Vault in Edit. */
internal fun CaseScope.openFormatting() {
    openPlaygroundNote(name = FORMATTING, shownText = FORMATTING_TEXT)
}

/** The open note's text field, Formatting practice unless [part] says another. */
internal fun CaseScope.editor(part: String = FORMATTING_TEXT): SemanticsNodeInteraction = noteText(part = part)

/** The open note's text as the editor holds it. */
internal fun CaseScope.editorText(part: String = FORMATTING_TEXT): String =
    editor(part = part).fetchSemanticsNode().config[SemanticsProperties.EditableText].text

/** Selects [words] in the note, as a person dragging over them, or the [range] given. */
internal fun CaseScope.select(
    words: String,
    part: String = FORMATTING_TEXT
) {
    val start = indexOf(words = words, part = part)
    selectRange(range = TextRange(start = start, end = start + words.length), part = part)
}

/** Puts the cursor right after [words] in the note, or before them with [before]. */
internal fun CaseScope.cursorAt(
    words: String,
    before: Boolean = false,
    part: String = FORMATTING_TEXT
) {
    val start = indexOf(words = words, part = part)
    selectRange(range = TextRange(index = if (before) start else start + words.length), part = part)
}

/** Puts the cursor at the very end of the note. */
internal fun CaseScope.cursorAtEnd(part: String = FORMATTING_TEXT) {
    selectRange(range = TextRange(index = editorText(part = part).length), part = part)
}

/** Waits until the note's text holds [text], as the editor shows it. */
internal fun CaseScope.expectInNote(
    text: String,
    part: String = FORMATTING_TEXT
) {
    val holds = runCatching {
        ui.waitUntil(timeoutMillis = 10_000) { runCatching { text in editorText(part = part) }.getOrDefault(false) }
    }.isSuccess
    if (!holds) throw CaseFailure(message = "expected the note to hold \"$text\", it reads \"${editorText(part)}\"")
}

/** Fails unless the note's selection is [words]. */
internal fun CaseScope.expectSelected(
    words: String,
    part: String = FORMATTING_TEXT
) {
    val node = editor(part = part).fetchSemanticsNode().config
    val range = node[SemanticsProperties.TextSelectionRange]
    val selected = node[SemanticsProperties.EditableText].text.substring(range.min, range.max)
    if (selected != words) throw CaseFailure(message = "expected \"$words\" selected, \"$selected\" is")
}

private fun CaseScope.indexOf(
    words: String,
    part: String
): Int {
    val index = editorText(part = part).indexOf(words)
    if (index < 0) throw CaseFailure(message = "expected \"$words\" in the note, it is not there")
    return index
}

private fun CaseScope.selectRange(
    range: TextRange,
    part: String
) {
    val field = editor(part = part)
    field.performClick()
    field.performTextInputSelection(selection = range)
    ui.waitForIdle()
}

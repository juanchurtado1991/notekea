package app.notekea.editor.domain

import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.links.LinkResolver
import app.notekea.core.notes.domain.links.NoteLink
import app.notekea.core.notes.domain.links.relativeTo
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** The note as a person edits it: the toolbar's edits at a selection or the cursor, and typing over a selection. */
internal class FormattingSteps(var text: String) {
    private val formatter = MarkdownFormatter()
    private val listEditing = ListEditing()

    // Nothing selected and the cursor at the start: an empty range whose last is -1.
    private var selection = IntRange(
        start = 0,
        endInclusive = -1
    )
    val cursor: Int get() = selection.last + 1

    fun select(
        part: String,
        action: FormatAction
    ) {
        val start = text.indexOf(string = part)

        apply(
            start = start,
            end = start + part.length,
            action = action
        )
    }

    fun at(
        index: Int,
        action: FormatAction?
    ) {
        if (action == null) {
            selection = index..<index
        } else {
            apply(
                start = index,
                end = index,
                action = action
            )
        }
    }

    /** The cursor at the end of [line], then [key] pressed there, if any. */
    fun atEndOf(
        line: String,
        key: ListAction?
    ) {
        val end = text.indexOf(string = line) + line.length
        selection = end..<end

        key?.let { key(action = it) }
    }

    fun key(action: ListAction) {
        val edit = listEditing.edit(
            text = text,
            selectionStart = selection.first,
            selectionEnd = selection.last + 1,
            action = action
        ) ?: return

        text = text.replaceRange(
            startIndex = edit.start,
            endIndex = edit.end,
            replacement = edit.replacement
        )

        selection = edit.selectionStart..<edit.selectionEnd
    }

    fun lineAtCursor(): String {
        val start = text.lastIndexOf(
            char = '\n',
            startIndex = selection.first - 1
        ) + 1

        val end = text.indexOf(
            char = '\n',
            startIndex = selection.first
        ).takeIf {
            it >= 0
        } ?: text.length

        return text.substring(
            startIndex = start,
            endIndex = end
        )
    }

    fun atLineOf(
        line: String,
        action: FormatAction
    ) = at(
        index = text.indexOf(string = line) + 1,
        action = action
    )

    fun type(text: String) {
        this.text = this.text.replaceRange(
            range = selection,
            replacement = text
        )

        val end = selection.first + text.length
        selection = end..<end
    }

    fun selected(): String = text.substring(range = selection)

    fun lineWith(part: String): String = text.lines().first { it.contains(other = part) }

    fun rangeOfLines(
        first: String,
        last: String
    ): IntRange {
        val start = text.lastIndexOf(
            char = '\n',
            startIndex = text.indexOf(string = first)
        ) + 1

        val end = text.indexOf(
            char = '\n',
            startIndex = text.indexOf(string = last)
        )

        return start..<end
    }

    fun tag(name: String): FormatAction.Tag = FormatAction.Tag(name = name)

    private fun apply(
        start: Int,
        end: Int,
        action: FormatAction
    ) {
        val edit = formatter.format(
            text = text,
            selectionStart = start,
            selectionEnd = end,
            action = action
        )

        text = text.replaceRange(
            startIndex = edit.start,
            endIndex = edit.end,
            replacement = edit.replacement
        )

        selection = edit.selectionStart..<edit.selectionEnd
    }
}

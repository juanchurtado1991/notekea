package app.notekea.editor.domain

import app.notekea.core.notes.domain.TextEdit

/** What a rewrite of a note made outside the editor becomes on the unsaved text, when it can be carried. */
object CarriedEdit {
    /**
     * The change from [base] to [theirs], made again on [mine] ([base] with the person's own edits): the edit to
     * apply to [mine], or null when both reach the same part of [base], which only the person can settle.
     */
    fun of(
        base: String,
        theirs: String,
        mine: String
    ): TextEdit? {
        // A rewrite of the properties alone leaves nothing to carry, however the text was edited.
        if (base == theirs) {
            return TextEdit(
                start = 0,
                end = 0,
                replacement = "",
                selectionStart = 0,
                selectionEnd = 0
            )
        }

        val change = changeOf(
            before = base,
            after = theirs
        )

        val edit = changeOf(
            before = base,
            after = mine
        )

        // Changes that only touch are a clash too: a word typed right after a rewritten link would join the new link.
        val shift = when {
            change.end < edit.start -> 0
            edit.end < change.start -> mine.length - base.length
            else -> return null
        }

        val start = change.start + shift
        val end = change.end + shift

        return TextEdit(
            start = start,
            end = end,
            replacement = change.replacement,
            selectionStart = start + change.replacement.length,
            selectionEnd = start + change.replacement.length
        )
    }

    /** [before] became [after] by replacing its text from [start] until [end] with [replacement]. */
    private data class Change(
        val start: Int,
        val end: Int,
        val replacement: String
    )

    private fun changeOf(
        before: String,
        after: String
    ): Change {
        val prefix = before.commonPrefixWith(other = after).length

        val suffix = before
            .substring(startIndex = prefix)
            .commonSuffixWith(other = after.substring(startIndex = prefix))
            .length

        return Change(
            start = prefix,
            end = before.length - suffix,
            replacement = after.substring(
                startIndex = prefix,
                endIndex = after.length - suffix
            )
        )
    }
}

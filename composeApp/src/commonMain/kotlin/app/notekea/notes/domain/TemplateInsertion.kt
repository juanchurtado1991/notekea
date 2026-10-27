package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns

/**
 * Where a filled-in template goes in a note (`ROADMAP.md` 1.12): its text replaces the selection (at the cursor when
 * nothing is selected), or the whole body when the note has no text yet, so an empty note becomes the template; the
 * cursor ends after it. Its frontmatter merges into the note's ([FrontmatterMerge]); a note without one takes the
 * template's with the note's own line breaks, so the file never mixes them.
 */
class TemplateInsertion {
    private val frontmatterMerge = FrontmatterMerge()

    fun insert(
        note: NoteDocument,
        selectionStart: Int,
        selectionEnd: Int,
        template: NoteDocument
    ): Result {
        val isEmpty = note.body.isBlank()
        val start = if (isEmpty) 0 else minOf(selectionStart, selectionEnd).coerceIn(0, note.body.length)
        val end = if (isEmpty) {
            note.body.length
        } else {
            maxOf(
                selectionStart,
                selectionEnd
            ).coerceIn(start, note.body.length)
        }
        val cursor = start + template.body.length
        val merged = frontmatterMerge.merge(noteHeader = note.header, templateHeader = template.header)
        return Result(
            header = if (note.header.isEmpty()) withLineBreaksOf(note = note, header = merged) else merged,
            edit = TextEdit(
                start = start,
                end = end,
                replacement = template.body,
                selectionStart = cursor,
                selectionEnd = cursor
            )
        )
    }

    private fun withLineBreaksOf(
        note: NoteDocument,
        header: String
    ): String {
        val unix = header.replace(oldValue = StringPatterns.WINDOWS_NEW_LINE, newValue = StringPatterns.NEW_LINE)
        return if (note.usesCrLf) unix.replace(StringPatterns.NEW_LINE, StringPatterns.WINDOWS_NEW_LINE) else unix
    }

    /** The note's new [header], and the [edit] to its body. */
    data class Result(
        val header: String,
        val edit: TextEdit
    )
}

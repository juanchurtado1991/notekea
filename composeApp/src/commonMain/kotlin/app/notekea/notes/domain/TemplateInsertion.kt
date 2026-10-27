package app.notekea.notes.domain

/**
 * Where a filled-in template goes in a note (`ROADMAP.md` 1.12): its text replaces the selection (at the cursor when
 * nothing is selected), or the whole body when the note has no text yet, so an empty note becomes the template; the
 * cursor ends after it. Its frontmatter merges into the note's ([FrontmatterMerge]).
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
        return Result(
            header = frontmatterMerge.merge(noteHeader = note.header, templateHeader = template.header),
            edit = TextEdit(
                start = start,
                end = end,
                replacement = template.body,
                selectionStart = cursor,
                selectionEnd = cursor
            )
        )
    }

    /** The note's new [header], and the [edit] to its body. */
    data class Result(
        val header: String,
        val edit: TextEdit
    )
}

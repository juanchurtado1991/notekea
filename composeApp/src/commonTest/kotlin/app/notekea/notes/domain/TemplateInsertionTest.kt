package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** Where a template goes in a note (`ROADMAP.md` 1.12): over the selection, or over a note with no text yet. */
class TemplateInsertionTest {
    private val insertion = TemplateInsertion()
    private val template = NoteDocument.fromText(text = "---\ntags: meeting\n---\n# Sync\n- [ ] \n")

    @Test
    fun `the template replaces the selection, the cursor after it, and its properties join the note's`() {
        val note = NoteDocument.fromText(text = "---\nstatus: draft\n---\nIntro\nold text\n")

        val result = insertion.insert(note = note, selectionStart = 6, selectionEnd = 14, template = template)

        assertEquals(
            expected = TemplateInsertion.Result(
                header = "---\nstatus: draft\ntags: meeting\n---\n",
                edit = TextEdit(
                    start = 6,
                    end = 14,
                    replacement = "# Sync\n- [ ] \n",
                    selectionStart = 20,
                    selectionEnd = 20
                )
            ),
            actual = result
        )
    }

    @Test
    fun `a note with only blank lines becomes the template whole, wherever the cursor is`() {
        val note = NoteDocument.fromText(text = "\n\n  \n")

        val result = insertion.insert(note = note, selectionStart = 2, selectionEnd = 2, template = template)

        assertEquals(
            expected = TextEdit(
                start = 0,
                end = 5,
                replacement = "# Sync\n- [ ] \n",
                selectionStart = 14,
                selectionEnd = 14
            ) to "---\ntags: meeting\n---\n",
            actual = result.edit to result.header
        )
    }
}

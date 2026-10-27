package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** A template's properties merged into a note's, as Obsidian merges them (`ROADMAP.md` 1.12). */
class FrontmatterMergeTest {
    private val merge = FrontmatterMerge()

    @Test
    fun `missing properties are added, existing ones keep their value, and lists gain the items they lack`() {
        val note = "---\nstatus: draft\ntags: [work, idea]\naliases:\n  - Plan\n---\n"
        val template = "---\nstatus: todo\ntype: meeting\ntags:\n  - meeting\n  - work\naliases: Plan\n---\n"

        assertEquals(
            expected = "---\nstatus: draft\ntags:\n  - work\n  - idea\n  - meeting\naliases:\n  - Plan\n" +
                "type: meeting\n---\n",
            actual = merge.merge(noteHeader = note, templateHeader = template)
        )
    }

    @Test
    fun `a note without properties takes the template's, and a template without any leaves the note as it is`() {
        val properties = "---\ntags: meeting\n---\n"

        assertEquals(
            expected = listOf(properties, properties, ""),
            actual = listOf(
                merge.merge(noteHeader = "", templateHeader = properties),
                merge.merge(noteHeader = properties, templateHeader = ""),
                merge.merge(noteHeader = "", templateHeader = "")
            )
        )
    }

    @Test
    fun `the note's line breaks, byte order mark, comments and nested values are kept`() {
        val note = "\uFEFF---\r\n# kept\r\nauthor:\r\n  name: Ana\r\n---\r\n"
        val template = "---\nauthor:\n  name: Template\nproject: Notekea\n---\n"

        assertEquals(
            expected = "\uFEFF---\r\n# kept\r\nauthor:\r\n  name: Ana\r\nproject: Notekea\r\n---\r\n",
            actual = merge.merge(noteHeader = note, templateHeader = template)
        )
    }
}

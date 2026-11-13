package app.notekea.core.notes.domain

import app.notekea.core.notes.domain.frontmatter.FrontmatterMerge
import kotlin.test.Test
import kotlin.test.assertEquals

/** A template's properties merged into a note's, as Obsidian merges them. */
class FrontmatterMergeTest {
    private val merge = FrontmatterMerge()

    @Test
    fun `missing properties are added, existing ones keep their value, and lists gain the items they lack`() {
        val note = "---\nstatus: draft\ntags: [work, idea]\naliases:\n  - Plan\n---\n"

        val template =
            "---\nstatus: todo\ntype: meeting\ntags:\n  - meeting\n  - work\naliases: Plan\n---\n"

        assertEquals(
            expected =
            "---\nstatus: draft\ntags:\n  - work\n  - idea\n  - meeting\naliases:\n  - Plan\n" +
                "type: meeting\n---\n",
            actual = merge.merge(
                noteHeader = note,
                templateHeader = template
            )
        )
    }

    @Test
    fun `a note without properties takes the template's, and a template without any leaves the note as it is`() {
        val properties = "---\ntags: meeting\n---\n"

        assertEquals(
            expected = listOf(properties, properties, ""),
            actual = listOf(
                merge.merge(
                    noteHeader = "",
                    templateHeader = properties
                ),
                merge.merge(
                    noteHeader = properties,
                    templateHeader = ""
                ),
                merge.merge(
                    noteHeader = "",
                    templateHeader = ""
                )
            )
        )
    }

    @Test
    fun `the note's line breaks, byte order mark, comments and nested values are kept`() {
        val note = "\uFEFF---\r\n# kept\r\nauthor:\r\n  name: Ana\r\n---\r\n"
        val template = "---\nauthor:\n  name: Template\nproject: Notekea\n---\n"

        assertEquals(
            expected = "\uFEFF---\r\n# kept\r\nauthor:\r\n  name: Ana\r\nproject: Notekea\r\n---\r\n",
            actual = merge.merge(
                noteHeader = note,
                templateHeader = template
            )
        )
    }

    @Test
    fun `a list that gains items keeps each one as written, and a new one is quoted when YAML needs it`() {
        val note = "---\ntags:\n  - \"Foo: bar\"\n  - \"#a\"\n  - 'old'\n---\n"
        val template = "---\ntags:\n  - \"New: one\"\n  - \"#b\"\n  - old\n---\n"

        assertEquals(
            expected = "---\ntags:\n  - \"Foo: bar\"\n  - \"#a\"\n  - 'old'\n  - \"New: one\"\n  - \"#b\"\n---\n",
            actual = merge.merge(
                noteHeader = note,
                templateHeader = template
            )
        )
    }

    @Test
    fun `a list property the note holds as a nested value or text stays as it is`() {
        val nested = "---\ntags:\n  group: work\n---\n"
        val text = "---\naliases: |\n  first\n  second\n---\n"
        val template = "---\ntags:\n  - meeting\naliases:\n  - Plan\n---\n"

        assertEquals(
            expected = listOf(
                "---\ntags:\n  group: work\naliases:\n  - Plan\n---\n",
                "---\naliases: |\n  first\n  second\ntags:\n  - meeting\n---\n"
            ),
            actual = listOf(
                merge.merge(
                    noteHeader = nested,
                    templateHeader = template
                ),
                merge.merge(
                    noteHeader = text,
                    templateHeader = template
                )
            )
        )
    }
}

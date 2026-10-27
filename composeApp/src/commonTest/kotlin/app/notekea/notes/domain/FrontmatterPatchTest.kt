package app.notekea.notes.domain

import app.notekea.core.domain.time.CalendarDate
import kotlin.test.Test
import kotlin.test.assertEquals

/** Editing one property rewrites only its lines (`ROADMAP.md` 1.14), in the style it already had. */
class FrontmatterPatchTest {
    private val patch = FrontmatterPatch()
    private val header =
        "\uFEFF---\r\n# kept\r\ntitle: 'Old'\r\ntags: [a, b]\r\naliases:\r\n    - One\r\ndone: false\r\n---\r\n"

    @Test
    fun `a value keeps the property's quotes, list style and indentation, and the rest stays byte for byte`() {
        assertEquals(
            expected = listOf(
                "\uFEFF---\r\n# kept\r\ntitle: 'It''s new'\r\ntags: [a, b]\r\naliases:\r\n    - One\r\ndone: false\r\n---\r\n",
                "\uFEFF---\r\n# kept\r\ntitle: 'Old'\r\ntags: [a, \"b: c\"]\r\naliases:\r\n    - One\r\ndone: false\r\n---\r\n",
                "\uFEFF---\r\n# kept\r\ntitle: 'Old'\r\ntags: [a, b]\r\naliases:\r\n    - One\r\n    - Two\r\ndone: false\r\n---\r\n",
                "\uFEFF---\r\n# kept\r\ntitle: 'Old'\r\ntags: [a, b]\r\naliases:\r\n    - One\r\ndone: true\r\n---\r\n"
            ),
            actual = listOf(
                patch.set(header = header, key = "title", value = PropertyValue.Text(text = "It's new")),
                patch.set(header = header, key = "tags", value = PropertyValue.Items(items = listOf("a", "b: c"))),
                patch.set(header = header, key = "aliases", value = PropertyValue.Items(items = listOf("One", "Two"))),
                patch.set(header = header, key = "done", value = PropertyValue.Checkbox(isChecked = true))
            )
        )
    }

    @Test
    fun `values that would read back as another type are quoted, and plain text is not`() {
        val plain = "---\nstatus: draft\n---\n"

        assertEquals(
            expected = listOf("\"true\"", "\"42\"", "\"2026-10-27\"", "\"#tag\"", "in progress", "2026-10-27", "7"),
            actual = listOf(
                PropertyValue.Text(text = "true"),
                PropertyValue.Text(text = "42"),
                PropertyValue.Text(text = "2026-10-27"),
                PropertyValue.Text(text = "#tag"),
                PropertyValue.Text(text = "in progress"),
                PropertyValue.Date(date = CalendarDate(year = 2026, month = 10, day = 27)),
                PropertyValue.Number(text = "7")
            ).map { patch.set(header = plain, key = "status", value = it).lines()[1].removePrefix("status: ") }
        )
    }

    @Test
    fun `a property is added, renamed and removed, and the last one takes the frontmatter along`() {
        val added = patch.add(header = "", key = "status", value = PropertyValue.Text(text = "draft"))
        val more = patch.add(header = added, key = "tags", value = PropertyValue.Items(items = listOf("work")))
        val renamed = patch.rename(header = more, from = "status", to = "state")

        assertEquals(
            expected = listOf(
                "---\nstatus: draft\n---\n",
                "---\nstatus: draft\ntags:\n  - work\n---\n",
                "---\nstate: draft\ntags:\n  - work\n---\n",
                "---\ntags:\n  - work\n---\n",
                ""
            ),
            actual = listOf(
                added,
                more,
                renamed,
                patch.remove(header = renamed, key = "state"),
                patch.remove(header = patch.remove(header = renamed, key = "state"), key = "tags")
            )
        )
    }
}

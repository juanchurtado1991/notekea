package app.notekea.core.notes.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.frontmatter.FrontmatterContent
import app.notekea.core.notes.domain.frontmatter.FrontmatterPatch
import app.notekea.core.notes.domain.frontmatter.FrontmatterReader
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlin.test.Test
import kotlin.test.assertEquals

/** Editing one property rewrites only its lines, in the style it already had. */
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
                patch.set(
                    header = header,
                    key = "title",
                    value = PropertyValue.Text(text = "It's new")
                ),
                patch.set(
                    header = header,
                    key = "tags",
                    value = PropertyValue.Items(items = listOf("a", "b: c"))
                ),
                patch.set(
                    header = header,
                    key = "aliases",
                    value = PropertyValue.Items(items = listOf("One", "Two"))
                ),
                patch.set(
                    header = header,
                    key = "done",
                    value = PropertyValue.Checkbox(isChecked = true)
                )
            )
        )
    }

    @Test
    fun `values that would read back as another type are quoted, and plain text is not`() {
        val plain = "---\nstatus: draft\n---\n"

        assertEquals(
            expected = listOf(
                "\"true\"",
                "\"42\"",
                "\"2026-10-27\"",
                "\"#tag\"",
                "in progress",
                "2026-10-27",
                "7"
            ),
            actual = listOf(
                PropertyValue.Text(text = "true"),
                PropertyValue.Text(text = "42"),
                PropertyValue.Text(text = "2026-10-27"),
                PropertyValue.Text(text = "#tag"),
                PropertyValue.Text(text = "in progress"),
                PropertyValue.Date(
                    date = CalendarDate(
                        year = 2026,
                        month = 10,
                        day = 27
                    )
                ),
                PropertyValue.Number(text = "7")
            ).map {
                patch
                    .set(
                        header = plain,
                        key = "status",
                        value = it
                    )
                    .lines()[1]
                    .removePrefix("status: ")
            }
        )
    }

    @Test
    fun `a property is added, renamed and removed, and the last one takes the frontmatter along`() {
        val added = patch.add(
            header = "",
            key = "status",
            value = PropertyValue.Text(text = "draft")
        )

        val more = patch.add(
            header = added,
            key = "tags",
            value = PropertyValue.Items(items = listOf("work"))
        )

        val renamed = patch.rename(
            header = more,
            from = "status",
            to = "state"
        )

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
                patch.remove(
                    header = renamed,
                    key = "state"
                ),
                patch.remove(
                    header = patch.remove(
                        header = renamed,
                        key = "state"
                    ),
                    key = "tags"
                )
            )
        )
    }

    @Test
    fun `an item with a comma added to an inline list is quoted, or it would read as two`() {
        val header = "---\nauthors: [\"Smith, John\", JS]\n---\n"

        assertEquals(
            expected = "---\nauthors: [\"Smith, John\", JS, \"Doe, Jane\"]\n---\n",
            actual = patch.set(
                header = header,
                key = "authors",
                value = PropertyValue.Items(items = listOf("Smith, John", "JS", "Doe, Jane"))
            )
        )
    }

    // Bug hunt A2: an item added to a quoted scalar wrote `tags: "work", "x"`, which is no YAML.
    @Test
    fun `a value on the key's line becomes an inline list once it has two items, unless it is plain tags`() {
        val values = listOf(
            "tags: \"work\"" to listOf("work", "x"),
            "author: Ana" to listOf("Ana", "Bob"),
            "tags: work" to listOf("work", "x")
        )

        assertEquals(
            expected = listOf(
                "tags: [\"work\", \"x\"]",
                "author: [Ana, Bob]",
                "tags: work, x"
            ),
            actual = values.map { (line, items) ->
                val key = line.substringBefore(delimiter = ":")

                patch
                    .set(
                        header = "---\n$line\n---\n",
                        key = key,
                        value = PropertyValue.Items(items = items)
                    )
                    .lines()[1]
            }
        )
    }

    // Bug hunt D10: editing a property dropped the comment at the end of its line.
    @Test
    fun `a comment at the end of a property's line stays when its value is edited`() {
        val header = "---\nstatus: draft # for now\ntags: [a] # mine\n---\n"

        val edited = patch.set(
            header = patch.set(
                header = header,
                key = "status",
                value = PropertyValue.Text(text = "done")
            ),
            key = "tags",
            value = PropertyValue.Items(items = listOf("a", "b"))
        )

        assertEquals(
            expected = "---\nstatus: done # for now\ntags: [a, b] # mine\n---\n",
            actual = edited
        )
    }

    // Seen on a blind evaluation vault (2026-11-17): writing back the value a property already has rewrote
    // `record: off` as `record: "off"`, a frontmatter changed though nothing in it did.
    @Test
    fun `a value set as it already is keeps the property as written, unquoted words included`() {
        val written = "---\nrecord: off\nstatus: yes\n---\n"
        val reader = FrontmatterReader()

        val properties = (reader.read(header = written) as FrontmatterContent.Properties).properties

        assertEquals(
            expected = listOf(written, written),
            actual = properties.map {
                patch.set(
                    header = written,
                    key = it.key,
                    value = it.value
                )
            }
        )
    }
}

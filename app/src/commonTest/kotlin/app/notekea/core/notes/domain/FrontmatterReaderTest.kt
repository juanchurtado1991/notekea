package app.notekea.core.notes.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.frontmatter.FrontmatterContent
import app.notekea.core.notes.domain.frontmatter.FrontmatterReader
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import app.notekea.core.notes.domain.frontmatter.YamlValues
import kotlin.test.Test
import kotlin.test.assertEquals

/** A note's frontmatter read as typed properties, as Obsidian types them. */
class FrontmatterReaderTest {
    private val reader = FrontmatterReader()

    @Test
    fun `each property gets the type its text shows`() {
        val header = TYPED_HEADER

        assertEquals(
            expected = FrontmatterContent.Properties(
                properties = listOf(
                    NoteProperty(
                        key = "title",
                        value = PropertyValue.Text(text = "Pricing: ideas")
                    ),
                    NoteProperty(
                        key = "status",
                        value = PropertyValue.Text(text = "draft")
                    ),
                    NoteProperty(
                        key = "done",
                        value = PropertyValue.Checkbox(isChecked = true)
                    ),
                    NoteProperty(
                        key = "rating",
                        value = PropertyValue.Number(text = "4.5")
                    ),
                    NoteProperty(
                        key = "due",
                        value = PropertyValue.Date(date = CalendarDate(2026, 10, 27))
                    ),
                    NoteProperty(
                        key = "tags",
                        value = PropertyValue.Items(items = listOf("work", "idea"))
                    ),
                    NoteProperty(
                        key = "aliases",
                        value = PropertyValue.Items(items = listOf("Plan", "The plan"))
                    ),
                    NoteProperty(
                        key = "cssclasses",
                        value = PropertyValue.Items(items = listOf("wide", "dark"))
                    ),
                    NoteProperty(
                        key = "author",
                        value = PropertyValue.Other(source = "author:\n  name: Ana")
                    ),
                    NoteProperty(
                        key = "summary",
                        value = PropertyValue.Other(source = "summary: |\n  Two lines")
                    ),
                    NoteProperty(
                        key = "empty",
                        value = PropertyValue.Text(text = "")
                    )
                )
            ),
            actual = reader.read(header = header)
        )
    }

    // The code review of 2026-11-13: "rating: 4 # of 5" was the text "4 # of 5", no number.
    @Test
    fun `a comment after a value is no part of it, a hash inside quotes or at its start is`() {
        val values =
            listOf("4 # of 5", "true   # for now", "\"Issue #12\" # the ticket", "#inbox", "C#")

        assertEquals(
            expected = listOf("4", "true", "\"Issue #12\"", "#inbox", "C#"),
            actual = values.map { YamlValues.withoutComment(text = it) }
        )
    }

    @Test
    fun `no frontmatter, a stray line and a repeated key are told apart`() {
        assertEquals(
            expected = listOf(
                FrontmatterContent.None,
                FrontmatterContent.Invalid(line = 2),
                FrontmatterContent.Invalid(line = 2),
                FrontmatterContent.Properties(properties = emptyList())
            ),
            actual = listOf(
                reader.read(header = ""),
                reader.read(header = "---\ntitle: A\nnot a property\n---\n"),
                reader.read(header = "---\ntitle: A\ntitle: B\n---\n"),
                reader.read(header = "---\n---\n")
            )
        )
    }

    companion object {
        private val TYPED_HEADER = """
            |---
            |# a comment
            |title: "Pricing: ideas"
            |status: draft
            |done: true
            |rating: 4.5
            |due: 2026-10-27
            |tags: [work, idea]
            |aliases:
            |  - Plan
            |  - 'The plan'
            |cssclasses: wide, dark
            |author:
            |  name: Ana
            |summary: |
            |  Two lines
            |empty:
            |---
            |
        """.trimMargin()
    }
}

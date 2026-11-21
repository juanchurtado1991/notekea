package app.notekea.query.domain

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlin.test.Test
import kotlin.test.assertEquals

/** Queries over properties as people write them by hand: in any case, with `null`, with a time after the day. */
class RunQueryWrittenTest {
    private val run = RunQueryUseCase()

    @Test
    fun `a property written null or a tilde holds nothing`() {
        val notes = listOf(
            note("Books/Dune.md", text("rating", "null")),
            note("Books/Emma.md", text("rating", "~")),
            note("Books/Sapiens.md", NoteProperty("rating", PropertyValue.Number("4")))
        )

        assertEquals(
            expected = listOf("Dune", "Emma"),
            actual = namesOf(notes, filter(QueryCondition.IS_EMPTY, "rating", ""))
        )

        assertEquals(
            expected = listOf("Sapiens"),
            actual = namesOf(notes, filter(QueryCondition.IS, "rating", ""))
        )
    }

    @Test
    fun `a property written null sorts with the notes that lack it`() {
        val notes = listOf(
            note("Books/Alpha.md", text("rating", "null")),
            note("Books/Beta.md", NoteProperty("rating", PropertyValue.Number("2"))),
            note("Books/Gamma.md", NoteProperty("rating", PropertyValue.Number("1")))
        )

        val rows = run(VaultQuery(sort = QuerySort(property = "rating")), notes).rows

        assertEquals(
            expected = listOf("Gamma", "Beta", "Alpha"),
            actual = rows.map { it.path.nameWithoutExtension }
        )
    }

    @Test
    fun `a date with a time compares by its day`() {
        val notes = listOf(
            note("Dune.md", text("due", "2026-01-05T10:00")),
            note("Emma.md", text("due", "2026-03-01 09:30")),
            note("Plan.md", text("due", "2026-01-05 notes"))
        )

        assertEquals(
            expected = listOf("Dune"),
            actual = namesOf(notes, filter(QueryCondition.BEFORE, "due", "2026-02-01"))
        )

        assertEquals(
            expected = listOf("Emma"),
            actual = namesOf(notes, filter(QueryCondition.AFTER, "due", "2026-02-01T00:00"))
        )

        assertEquals(
            expected = listOf("Dune"),
            actual = namesOf(notes, filter(QueryCondition.IS, "due", "2026-01-05"))
        )
    }

    @Test
    fun `property names match whatever their case`() {
        val notes = listOf(
            note("Dune.md", text("Status", "done")),
            note("Emma.md", text("status", "open")),
            note("Plan.md", text("STATUS", "active"))
        )

        val query = VaultQuery(
            filters = listOf(filter(QueryCondition.CONTAINS, "status", "o")),
            sort = QuerySort(property = "Status"),
            columns = listOf("status")
        )

        val result = run(query, notes)

        assertEquals(
            expected = listOf("Dune", "Emma"),
            actual = result.rows.map { it.path.nameWithoutExtension }
        )

        assertEquals(
            expected = listOf(text("Status", "done"), text("status", "open")),
            actual = result.rows.map { it.cells.single() }
        )

        assertEquals(
            expected = emptyList(),
            actual = result.unknown
        )
    }

    @Test
    fun `a folder matches whatever its case, only at its end`() {
        val notes = listOf(
            note("Books/Dune.md"),
            note("Bookshelf/Emma.md"),
            note("Plan.md")
        )

        assertEquals(
            expected = listOf("Dune"),
            actual = namesOf(notes, QueryFilter(QueryCondition.IN_FOLDER, value = "books"))
        )
    }

    private fun filter(
        condition: QueryCondition,
        property: String,
        value: String
    ) = QueryFilter(
        condition = condition,
        property = property,
        value = value
    )

    private fun namesOf(
        notes: List<NoteEntry>,
        filter: QueryFilter
    ): List<String> = run(VaultQuery(filters = listOf(filter)), notes).rows.map {
        it.path.nameWithoutExtension
    }

    private companion object {
        fun text(key: String, text: String) = NoteProperty(key, PropertyValue.Text(text))

        fun note(
            path: String,
            vararg properties: NoteProperty
        ) = NoteEntry(
            file = NoteFile(
                path = RelativePath(value = path),
                size = null,
                modifiedAt = null
            ),
            metadata = NoteMetadata(
                tags = emptyList(),
                aliases = emptyList(),
                title = null,
                textTags = emptyList(),
                properties = properties.toList()
            )
        )
    }
}

package app.notekea.query.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

class RunQueryUseCaseTest {
    private val run = RunQueryUseCase()

    @Test
    fun `numbers compare by value, not as text`() {
        assertEquals(
            expected = listOf("Dune", "Sapiens"),
            actual = namesOf(filter(QueryCondition.MORE_THAN, "rating", "9"))
        )
        assertEquals(expected = listOf("Emma"), actual = namesOf(filter(QueryCondition.LESS_THAN, "rating", "9")))
        assertEquals(expected = listOf("Dune"), actual = namesOf(filter(QueryCondition.IS, "rating", "10.0")))
    }

    @Test
    fun `dates compare as days`() {
        assertEquals(expected = listOf("Dune"), actual = namesOf(filter(QueryCondition.AFTER, "read", "2026-03-01")))
        assertEquals(expected = listOf("Emma"), actual = namesOf(filter(QueryCondition.BEFORE, "read", "2026-03-02")))
    }

    @Test
    fun `text and lists match whole or in part, ignoring case`() {
        assertEquals(expected = listOf("Dune"), actual = namesOf(filter(QueryCondition.IS, "author", "frank herbert")))
        assertEquals(expected = listOf("Dune"), actual = namesOf(filter(QueryCondition.CONTAINS, "author", "HERB")))
        assertEquals(expected = listOf("Emma"), actual = namesOf(filter(QueryCondition.IS, "genres", "Romance")))
        assertEquals(expected = listOf("Sapiens"), actual = namesOf(filter(QueryCondition.IS, "finished", "false")))
    }

    @Test
    fun `is empty takes notes without the property, or with nothing in it`() {
        assertEquals(
            expected = listOf("Emma", "Plan", "Sapiens"),
            actual = namesOf(filter(QueryCondition.IS_EMPTY, "author", ""))
        )
    }

    @Test
    fun `a folder takes the notes inside it, a tag its nested ones and the ones in the text`() {
        assertEquals(
            expected = listOf("Dune", "Emma", "Sapiens"),
            actual = namesOf(QueryFilter(QueryCondition.IN_FOLDER, value = "Books/"))
        )
        assertEquals(
            expected = listOf("Dune", "Plan"),
            actual = namesOf(QueryFilter(QueryCondition.HAS_TAG, value = "#space"))
        )
    }

    @Test
    fun `every filter must pass`() {
        val query = VaultQuery(
            filters = listOf(
                filter(QueryCondition.MORE_THAN, "rating", "1"),
                filter(QueryCondition.CONTAINS, "author", "frank")
            )
        )

        assertEquals(expected = listOf("Dune"), actual = namesOf(query))
    }

    @Test
    fun `rows sort by the property, notes without it last, and by name otherwise`() {
        assertEquals(
            expected = listOf("Dune", "Sapiens", "Emma", "Plan"),
            actual = namesOf(VaultQuery(sort = QuerySort(property = "rating", isDescending = true)))
        )
        assertEquals(
            expected = listOf("Emma", "Sapiens", "Dune", "Plan"),
            actual = namesOf(VaultQuery(sort = QuerySort(property = "rating")))
        )
        assertEquals(expected = listOf("Dune", "Emma", "Plan", "Sapiens"), actual = namesOf(VaultQuery()))
    }

    @Test
    fun `a sort over mixed types keeps one order`() {
        val notes = listOf("1.5", "1.10").map { note("Books/$it.md", number("v", it)) } +
            note("Books/text.md", text("v", "1.7"))

        val rows = run(VaultQuery(sort = QuerySort(property = "v")), notes).rows

        assertEquals(
            expected = listOf("1.10", "1.5", "text"),
            actual = rows.map { it.path.nameWithoutExtension }
        )
    }

    @Test
    fun `cells follow the columns, the query's own properties when none are chosen`() {
        val chosen = run(VaultQuery(columns = listOf("rating", "due")), listOf(NOTES.first()))
        val derived = run(VaultQuery(filters = listOf(filter(QueryCondition.IS, "rating", "10"))), NOTES)

        assertEquals(
            expected = listOf(NoteProperty("rating", PropertyValue.Number("10")), null),
            actual = chosen.rows.single().cells
        )
        assertEquals(expected = listOf("rating"), actual = derived.columns)
    }

    @Test
    fun `a property no note has is told apart from a query with no matches`() {
        val query = VaultQuery(filters = listOf(filter(QueryCondition.IS, "mood", "calm")), columns = listOf("author"))

        assertEquals(expected = listOf("mood"), actual = run(query, NOTES).unknown)
    }

    private fun filter(
        condition: QueryCondition,
        property: String,
        value: String
    ) = QueryFilter(condition = condition, property = property, value = value)

    private fun namesOf(
        filter: QueryFilter
    ): List<String> = namesOf(VaultQuery(filters = listOf(filter)))

    private fun namesOf(
        query: VaultQuery
    ): List<String> = run(query, NOTES).rows.map { it.path.nameWithoutExtension }

    private companion object {
        fun number(key: String, text: String) = NoteProperty(key, PropertyValue.Number(text))

        fun text(key: String, text: String) = NoteProperty(key, PropertyValue.Text(text))

        fun date(key: String, day: Int) = NoteProperty(key, PropertyValue.Date(CalendarDate(2026, 3, day)))

        fun note(
            path: String,
            vararg properties: NoteProperty,
            tags: List<String> = emptyList(),
            textTags: List<String> = emptyList()
        ) = NoteEntry(
            file = NoteFile(path = RelativePath(value = path), size = null, modifiedAt = null),
            metadata = NoteMetadata(
                tags = tags,
                aliases = emptyList(),
                title = null,
                textTags = textTags,
                properties = properties.toList()
            )
        )

        val NOTES = listOf(
            note(
                "Books/Dune.md",
                number("rating", "10"),
                text("author", "Frank Herbert"),
                date("read", 2),
                tags = listOf("space/opera")
            ),
            note(
                "Books/Sapiens.md",
                number("rating", "9.5"),
                text("author", ""),
                NoteProperty("finished", PropertyValue.Checkbox(false))
            ),
            note(
                "Books/Emma.md",
                number("rating", "2"),
                date("read", 1),
                NoteProperty("genres", PropertyValue.Items(listOf("classic", "romance")))
            ),
            note("Plan.md", textTags = listOf("space"))
        )
    }
}

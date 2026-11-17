package app.notekea.query.domain

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildQueryCatalogUseCaseTest {
    @Test
    fun `two spellings of a name are one property, under the spelling most notes use`() {
        val notes = listOf("Status", "status", "status").mapIndexed { index, key ->
            note(
                path = "Note$index.md",
                property = NoteProperty(key, PropertyValue.Text("open"))
            )
        }

        assertEquals(
            expected = listOf(
                QueryKey(
                    name = "status",
                    kind = QueryValueKind.TEXT
                )
            ),
            actual = BuildQueryCatalogUseCase()(notes).keys
        )
    }

    private fun note(
        path: String,
        property: NoteProperty
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
            properties = listOf(property)
        )
    )
}

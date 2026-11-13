package app.notekea.query.domain

import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** What a query writes to notes: a property from its table, and itself as a block. */
class QueryNoteWritesTest {
    private val store = FakeNoteContentStore()
    private val apply = ApplyLinkUpdatesUseCase(noteContentStore = store)
    private val setProperty = SetPropertyUseCase(
        noteContentStore = store,
        applyLinkUpdates = apply
    )
    private val saveQuery = SaveQueryUseCase(store, apply, QueryYaml())

    @Test
    fun `a property changes in place and the rest of the note stays`() = runTest {
        store.texts[NOTE] = "---\n# mine\nrating: 3\nauthor: 'Ana'\n---\nDune\n"

        setProperty(
            VAULT,
            NOTE,
            NoteProperty(
                key = "rating",
                value = PropertyValue.Number(text = "5")
            )
        )

        assertEquals(
            expected = "---\n# mine\nrating: 5\nauthor: 'Ana'\n---\nDune\n",
            actual = store.texts[NOTE]
        )
    }

    @Test
    fun `a property the note lacks is added, with a frontmatter if it had none`() = runTest {
        store.texts[NOTE] = "Dune\n"

        setProperty(
            VAULT,
            NOTE,
            NoteProperty(
                key = "rating",
                value = PropertyValue.Number(text = "5")
            )
        )

        assertEquals(
            expected = "---\nrating: 5\n---\nDune\n",
            actual = store.texts[NOTE]
        )
    }

    @Test
    fun `a value the note already holds is no second key`() = runTest {
        store.texts[NOTE] = "---\nfinished: true\n---\nDune\n"

        setProperty(
            VAULT,
            NOTE,
            NoteProperty(
                key = "finished",
                value = PropertyValue.Checkbox(isChecked = true)
            )
        )

        assertEquals(
            expected = "---\nfinished: true\n---\nDune\n",
            actual = store.texts[NOTE]
        )
    }

    @Test
    fun `a frontmatter added to a note with Windows line breaks uses them`() = runTest {
        store.texts[NOTE] = "Dune\r\nHerbert\r\n"

        setProperty(
            VAULT,
            NOTE,
            NoteProperty(
                key = "rating",
                value = PropertyValue.Number(text = "5")
            )
        )

        assertEquals(
            expected = "---\r\nrating: 5\r\n---\r\nDune\r\nHerbert\r\n",
            actual = store.texts[NOTE]
        )
    }

    @Test
    fun `a note that cannot be read is left alone`() = runTest {
        val rewrite = setProperty(
            VAULT,
            NOTE,
            NoteProperty(
                key = "rating",
                value = PropertyValue.Number(text = "5")
            )
        )

        assertNull(actual = rewrite)
    }

    @Test
    fun `a query is saved at the end of the note as a block`() = runTest {
        store.texts[NOTE] = "---\ntags: [a]\n---\nDune\n\n"
        val query = VaultQuery(columns = listOf("rating"))

        saveQuery(VAULT, NOTE, query)

        assertEquals(
            expected = "---\ntags: [a]\n---\nDune\n\n```query\ncolumns: [rating]\n```\n",
            actual = store.texts[NOTE]
        )
    }

    @Test
    fun `a query the note already holds is not saved twice, and another one is`() = runTest {
        store.texts[NOTE] = "Dune\n"
        val rated = VaultQuery(columns = listOf("rating"))

        repeat(times = 5) { saveQuery(VAULT, NOTE, rated) }

        saveQuery(VAULT, NOTE, VaultQuery(columns = listOf("author")))

        assertEquals(
            expected = "Dune\n\n```query\ncolumns: [rating]\n```\n\n```query\ncolumns: [author]\n```\n",
            actual = store.texts[NOTE]
        )
    }

    @Test
    fun `an empty query in an empty note is a block with nothing in it`() = runTest {
        store.texts[NOTE] = ""

        saveQuery(VAULT, NOTE, VaultQuery())

        assertEquals(
            expected = "```query\n```\n",
            actual = store.texts[NOTE]
        )
    }

    private companion object {
        const val VAULT = "/vault"
        val NOTE = RelativePath(value = "Books/Dune.md")
    }
}

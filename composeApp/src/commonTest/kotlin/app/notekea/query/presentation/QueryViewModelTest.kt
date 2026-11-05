package app.notekea.query.presentation

import androidx.lifecycle.viewModelScope
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.domain.RelativePath
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryKey
import app.notekea.query.domain.QueryPreferencesStore
import app.notekea.query.domain.QuerySort
import app.notekea.query.domain.QueryValueKind
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SaveQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.VaultQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QueryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = FakeNoteContentStore()
    private val preferences = KeptQuery()
    private val viewModel by lazy {
        val apply = ApplyLinkUpdatesUseCase(noteContentStore = store)
        QueryViewModel(
            vaultPath = VAULT,
            runQuery = RunQueryUseCase(),
            buildCatalog = BuildQueryCatalogUseCase(),
            setProperty = SetPropertyUseCase(noteContentStore = store, applyLinkUpdates = apply),
            saveQuery = SaveQueryUseCase(store, apply, QueryYaml()),
            preferencesStore = preferences,
            dispatchers = FakeAppDispatchers(dispatcher = dispatcher)
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterTest
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    private fun test(
        block: suspend TestScope.() -> Unit
    ) = runTest(context = dispatcher, testBody = block)

    private fun TestScope.show() {
        viewModel.onIntent(intent = QueryIntent.OnNotesChange(notes = NOTES))
        viewModel.onIntent(intent = QueryIntent.OnShownChange(isShown = true))
        advanceUntilIdle()
    }

    private fun names(): List<String> = viewModel.state.value.result.rows.map { it.path.nameWithoutExtension }

    @Test
    fun `nothing runs until the tab shows, and then it lists the notes and what they offer`() = test {
        viewModel.onIntent(intent = QueryIntent.OnNotesChange(notes = NOTES))
        advanceUntilIdle()
        val before = viewModel.state.value.isLoading

        show()

        assertTrue(actual = before)
        assertEquals(expected = listOf("Dune", "Emma"), actual = names())
        assertEquals(
            expected = listOf(QueryKey(name = "rating", kind = QueryValueKind.NUMBER)),
            actual = viewModel.state.value.catalog.keys
        )
    }

    @Test
    fun `a closed tab stops following the vault`() = test {
        show()
        viewModel.onIntent(intent = QueryIntent.OnShownChange(isShown = false))
        viewModel.onIntent(intent = QueryIntent.OnNotesChange(notes = NOTES.take(n = 1)))
        advanceUntilIdle()

        assertEquals(expected = listOf("Dune", "Emma"), actual = names())
    }

    @Test
    fun `the last column shown can be hidden`() = test {
        val rated = QueryFilter(condition = QueryCondition.MORE_THAN, property = "rating", value = "1")
        show()
        viewModel.onIntent(intent = QueryIntent.OnFilterAdd(filter = rated))
        viewModel.onIntent(intent = QueryIntent.OnColumnToggle(column = "rating"))
        advanceUntilIdle()

        assertEquals(expected = emptyList(), actual = viewModel.state.value.result.columns)
    }

    @Test
    fun `a block runs on the note a cell just wrote, before the scan`() = test {
        store.texts[DUNE] = "---\nrating: 5\n---\nDune\n"
        show()
        val version = viewModel.state.value.notesVersion
        viewModel.onIntent(intent = QueryIntent.OnCellChange(path = DUNE, property = rating(text = "1")))
        advanceUntilIdle()

        val first = viewModel.resultOf(query = VaultQuery(columns = listOf("rating"))).rows.first()

        assertTrue(actual = viewModel.state.value.notesVersion > version)
        assertEquals(expected = rating(text = "1"), actual = first.cells.single())
    }

    // Smoke: the Folder menu opened with an empty row, the vault's root.
    @Test
    fun `the vault's root is no folder to choose`() = test {
        val file = NoteFile(path = RelativePath(value = "Inbox.md"), size = null, modifiedAt = null)
        val inbox = NoteEntry(file = file, metadata = NoteMetadata.NONE)
        viewModel.onIntent(intent = QueryIntent.OnNotesChange(notes = NOTES + inbox))
        viewModel.onIntent(intent = QueryIntent.OnShownChange(isShown = true))
        advanceUntilIdle()

        assertEquals(expected = listOf("Books"), actual = viewModel.state.value.catalog.folders)
    }

    // Smoke: five clicks on Save in Reading list left five blocks in the note.
    @Test
    fun `clicking Save again and again leaves one block and says it is there`() = test {
        store.texts[DUNE] = "Dune\n"
        show()
        repeat(times = 5) { viewModel.onIntent(intent = QueryIntent.OnSave(path = DUNE)) }
        advanceUntilIdle()

        assertEquals(expected = 1, actual = store.texts.getValue(DUNE).split("```query").size - 1)
        assertEquals(expected = QueryNotice.AlreadySaved(path = DUNE), actual = viewModel.state.value.notice)
    }

    @Test
    fun `a note about a save goes once the query changes`() = test {
        store.texts[DUNE] = "Dune\n"
        show()
        viewModel.onIntent(intent = QueryIntent.OnSave(path = DUNE))
        advanceUntilIdle()
        viewModel.onIntent(intent = QueryIntent.OnColumnToggle(column = "rating"))
        advanceUntilIdle()

        assertEquals(expected = null, actual = viewModel.state.value.notice)
    }

    @Test
    fun `the result counts the vault's notes, not only those found`() = test {
        show()
        viewModel.onIntent(
            intent = QueryIntent.OnFilterAdd(
                filter = QueryFilter(condition = QueryCondition.MORE_THAN, property = "rating", value = "3")
            )
        )
        advanceUntilIdle()

        assertEquals(expected = 1 to 2, actual = viewModel.state.value.result.let { it.rows.size to it.total })
    }

    @Test
    fun `the vault's change reaches the table`() = test {
        show()

        viewModel.onIntent(intent = QueryIntent.OnNotesChange(notes = NOTES.take(n = 1)))
        advanceUntilIdle()

        assertEquals(expected = listOf("Dune"), actual = names())
    }

    @Test
    fun `filters are added, changed and removed, and the result follows each`() = test {
        show()
        val more = QueryFilter(condition = QueryCondition.MORE_THAN, property = "rating", value = "3")

        viewModel.onIntent(intent = QueryIntent.OnFilterAdd(filter = more))
        advanceUntilIdle()
        val added = names()
        viewModel.onIntent(intent = QueryIntent.OnFilterChange(index = 0, filter = more.copy(value = "9")))
        advanceUntilIdle()
        val changed = names()
        viewModel.onIntent(intent = QueryIntent.OnFilterRemove(index = 0))
        advanceUntilIdle()

        assertEquals(expected = listOf("Dune"), actual = added)
        assertEquals(expected = emptyList(), actual = changed)
        assertEquals(expected = listOf("Dune", "Emma"), actual = names())
    }

    @Test
    fun `the query is kept for the next run, and read back from it`() = test {
        preferences.query = VaultQuery(columns = listOf("rating"))
        show()

        viewModel.onIntent(intent = QueryIntent.OnSortChange(sort = QuerySort(property = "rating")))
        advanceUntilIdle()

        assertEquals(
            expected = VaultQuery(sort = QuerySort(property = "rating"), columns = listOf("rating")),
            actual = preferences.query
        )
        assertEquals(expected = listOf("Emma", "Dune"), actual = names())
    }

    @Test
    fun `choosing a column keeps the ones shown until then, and choosing it again hides it`() = test {
        preferences.query = VaultQuery(sort = QuerySort(property = "rating"))
        show()

        viewModel.onIntent(intent = QueryIntent.OnColumnToggle(column = "author"))
        advanceUntilIdle()
        val shown = viewModel.state.value.query.columns
        viewModel.onIntent(intent = QueryIntent.OnColumnToggle(column = "author"))
        advanceUntilIdle()

        assertEquals(expected = listOf("rating", "author"), actual = shown)
        assertEquals(expected = listOf("rating"), actual = viewModel.state.value.query.columns)
    }

    @Test
    fun `a cell writes its note, tells its editors and shows the value at once`() = test {
        preferences.query = VaultQuery(columns = listOf("rating"))
        store.texts[DUNE] = "---\nrating: 5\n---\nDune\n"
        show()
        val effects = mutableListOf<QueryEffect>()
        val collector = launch { viewModel.effects.collect { effects += it } }
        val changed = NoteProperty(key = "rating", value = PropertyValue.Number(text = "4"))

        viewModel.onIntent(intent = QueryIntent.OnCellChange(path = DUNE, property = changed))
        advanceUntilIdle()
        collector.cancel()

        assertEquals(expected = "---\nrating: 4\n---\nDune\n", actual = store.texts[DUNE])
        assertEquals(expected = listOf<QueryEffect>(QueryEffect.NoteRewritten(path = DUNE)), actual = effects)
        assertEquals(expected = changed, actual = viewModel.state.value.result.rows.first().cells.single())
    }

    @Test
    fun `a cell that cannot be written says so and keeps the old value`() = test {
        preferences.query = VaultQuery(columns = listOf("rating"))
        show()
        val before = viewModel.state.value.result

        viewModel.onIntent(
            intent = QueryIntent.OnCellChange(
                path = DUNE,
                property = NoteProperty(key = "rating", value = PropertyValue.Number(text = "4"))
            )
        )
        advanceUntilIdle()

        assertEquals(expected = QueryNotice.WriteFailed, actual = viewModel.state.value.notice)
        assertEquals(expected = before, actual = viewModel.state.value.result)
    }

    @Test
    fun `saving writes a block in the note and says where, until it is dismissed`() = test {
        preferences.query = VaultQuery(columns = listOf("rating"))
        store.texts[DUNE] = "Dune\n"
        show()

        viewModel.onIntent(intent = QueryIntent.OnSave(path = DUNE))
        advanceUntilIdle()
        val notice = viewModel.state.value.notice
        viewModel.onIntent(intent = QueryIntent.OnNoticeDismiss)

        assertEquals(expected = "Dune\n\n```query\ncolumns: [rating]\n```\n", actual = store.texts[DUNE])
        assertEquals(expected = QueryNotice.Saved(path = DUNE), actual = notice)
        assertEquals(expected = null, actual = viewModel.state.value.notice)
    }

    @Test
    fun `a note chosen in the table opens`() = test {
        val effects = mutableListOf<QueryEffect>()
        val collector = launch { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(intent = QueryIntent.OnNoteOpen(path = DUNE))
        advanceUntilIdle()
        collector.cancel()

        assertEquals(expected = listOf<QueryEffect>(QueryEffect.OpenNote(path = DUNE)), actual = effects)
    }

    private class KeptQuery : QueryPreferencesStore {
        var query = VaultQuery()

        override suspend fun read(
            vaultPath: String
        ): VaultQuery = query

        override suspend fun write(
            vaultPath: String,
            query: VaultQuery
        ) {
            this.query = query
        }
    }

    private companion object {
        const val VAULT = "/vault"
        val DUNE = RelativePath(value = "Books/Dune.md")

        fun note(
            path: RelativePath,
            rating: String
        ) = NoteEntry(
            file = NoteFile(path = path, size = null, modifiedAt = null),
            metadata = NoteMetadata.NONE.copy(
                properties = listOf(NoteProperty(key = "rating", value = PropertyValue.Number(text = rating)))
            )
        )

        fun rating(
            text: String
        ) = NoteProperty(key = "rating", value = PropertyValue.Number(text = text))

        val NOTES = listOf(note(path = DUNE, rating = "5"), note(path = RelativePath("Books/Emma.md"), rating = "2"))
    }
}

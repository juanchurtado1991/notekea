package app.notekea.query.presentation

import androidx.lifecycle.viewModelScope
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.domain.RelativePath
import app.notekea.query.data.GhostQueryJsonReader
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
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.query.domain.VaultQuery
import app.notekea.query.presentation.QueryTestData.DUNE
import app.notekea.query.presentation.QueryTestData.NOTES
import app.notekea.query.presentation.QueryTestData.VAULT
import app.notekea.query.presentation.QueryTestData.note
import app.notekea.query.presentation.QueryTestData.rating
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
import kotlin.time.Clock

/** "Describe what you want" (3.8) in the Query tab: the model's query, checked against the vault. */
@OptIn(ExperimentalCoroutinesApi::class)
class QueryDescribeTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = FakeNoteContentStore()
    private val preferences = KeptQuery()
    private val engine = FakeLlmEngine()
    private val viewModel by lazy {
        val apply = ApplyLinkUpdatesUseCase(noteContentStore = store)

        QueryViewModel(
            vaultPath = VAULT,
            runQuery = RunQueryUseCase(),
            buildCatalog = BuildQueryCatalogUseCase(),
            setProperty = SetPropertyUseCase(
                noteContentStore = store,
                applyLinkUpdates = apply
            ),
            saveQuery = SaveQueryUseCase(store, apply, QueryYaml()),
            preferencesStore = preferences,
            translateQuery = TranslateQueryUseCase(
                llmEngine = engine,
                reader = GhostQueryJsonReader()
            ),
            localCalendar = FakeLocalCalendar(),
            clock = Clock.System,
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

    private fun test(block: suspend TestScope.() -> Unit) = runTest(
        context = dispatcher,
        testBody = block
    )

    private fun TestScope.show() {
        viewModel.onIntent(intent = QueryIntent.OnNotesChange(notes = NOTES))
        viewModel.onIntent(intent = QueryIntent.OnShownChange(isShown = true))
        advanceUntilIdle()
    }

    @Test
    fun `a description fills the rules with the vault's spelling, and an unknown asks which was meant`() =
        test {
            show()

            engine.reply = { DESCRIBED }
            viewModel.onIntent(intent = QueryIntent.OnDescribe(request = "books I rated 4 or more"))
            advanceUntilIdle()
            val filled = viewModel.state.value.query.filters

            engine.reply = { UNKNOWN }
            viewModel.onIntent(intent = QueryIntent.OnDescribe(request = "books by stars"))
            advanceUntilIdle()
            val notice = viewModel.state.value.notice as? QueryNotice.UnknownProperty

            assertEquals(
                expected = listOf(
                    QueryFilter(
                        condition = QueryCondition.MORE_THAN,
                        property = "rating",
                        value = "3"
                    )
                ) to "stars",
                actual = filled to notice?.property
            )
        }

    private companion object {
        const val DESCRIBED =
            "{\"filters\":[{\"property\":\"Rating\",\"condition\":\"more than\",\"value\":\"3\"}]}"
        const val UNKNOWN =
            "{\"filters\":[{\"property\":\"stars\",\"condition\":\"is\",\"value\":\"5\"}]}"
    }
}

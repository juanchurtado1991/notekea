package app.notekea.query.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.query.domain.VaultQuery
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.input.key.Key
import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryResult
import kotlin.test.Test
import kotlin.test.assertEquals

/** The Query tab and its blocks on screen (`ROADMAP.md` 2.10). */
@OptIn(ExperimentalTestApi::class)
class QueryContentTest {
    private fun ComposeUiTest.showRules(
        state: QueryState,
        intents: MutableList<QueryIntent>
    ) = setContent {
        NotekeaTheme(darkTheme = false) {
            QueryContent(state = state, onIntent = { intents += it }, saveTarget = null)
        }
    }

    private fun ComposeUiTest.typeValue(
        from: String,
        to: String
    ) {
        onNode(matcher = hasSetTextAction() and hasText(text = from)).run {
            performClick()
            performTextReplacement(text = to)
        }
        onNode(matcher = hasSetTextAction() and hasText(text = to)).performKeyInput { pressKey(key = Key.Enter) }
        waitForIdle()
    }

    @Test
    fun `a rule's value changes its filter`() = runComposeUiTest {
        val intents = mutableListOf<QueryIntent>()
        showRules(state = QueryPreviewData.state, intents = intents)

        typeValue(from = "3", to = "4")

        assertEquals(
            expected = listOf<QueryIntent>(
                QueryIntent.OnFilterChange(index = 1, filter = QueryPreviewData.ratingFilter.copy(value = "4"))
            ),
            actual = intents
        )
    }

    @Test
    fun `a value that does not fit the property is refused`() = runComposeUiTest {
        val intents = mutableListOf<QueryIntent>()
        showRules(state = QueryPreviewData.state, intents = intents)

        typeValue(from = "3", to = "high")

        assertEquals(expected = emptyList(), actual = intents)
    }

    @Test
    fun `a new rule reaches the query only with its value`() = runComposeUiTest {
        val state = QueryPreviewData.state.copy(query = VaultQuery())
        val intents = mutableListOf<QueryIntent>()
        showRules(state = state, intents = intents)

        onNodeWithText(text = "Add filter").performClick()
        waitForIdle()
        onNodeWithText(text = "Notes matching all of these").assertExists()
        assertEquals(expected = emptyList(), actual = intents)
        onNodeWithText(text = "Choose…").performClick()
        waitForIdle()
        onNodeWithText(text = "Books").performClick()
        waitForIdle()

        assertEquals(
            expected = listOf<QueryIntent>(
                QueryIntent.OnFilterAdd(filter = QueryFilter(condition = QueryCondition.IN_FOLDER, value = "Books"))
            ),
            actual = intents
        )
    }

    @Test
    fun `a vault with no folders starts a rule on what it has`() = runComposeUiTest {
        val state = QueryPreviewData.state.copy(
            query = VaultQuery(),
            catalog = QueryPreviewData.state.catalog.copy(folders = emptyList(), tags = emptyList())
        )
        showRules(state = state, intents = mutableListOf())

        onNodeWithText(text = "Add filter").performClick()
        waitForIdle()

        onNodeWithText(text = "Value").assertExists()
        onNodeWithText(text = "Folder").assertDoesNotExist()
    }

    @Test
    fun `the foot says how many of the vault's notes were found`() = runComposeUiTest {
        showRules(state = QueryPreviewData.state, intents = mutableListOf())

        onNodeWithText(text = "1 of 40 notes").assertExists()
    }

    @Test
    fun `a name opens its note, and the note beside the tab can keep the query`() = runComposeUiTest {
        val intents = mutableListOf<QueryIntent>()
        val note = RelativePath(value = "Books/Reading list.md")
        setContent {
            NotekeaTheme(darkTheme = false) {
                QueryContent(state = QueryPreviewData.state, onIntent = { intents += it }, saveTarget = note)
            }
        }

        onNodeWithText(text = "Dune").performClick()
        onNodeWithText(text = "Save in Reading list").performClick()
        waitForIdle()

        assertEquals(
            expected = listOf(
                QueryIntent.OnNoteOpen(path = RelativePath(value = "Books/Dune.md")),
                QueryIntent.OnSave(path = note)
            ),
            actual = intents
        )
    }

    @Test
    fun `no match says so, and a property no note has is named`() = runComposeUiTest {
        val state = QueryPreviewData.state.copy(
            query = QueryPreviewData.state.query.copy(
                filters = listOf(QueryFilter(condition = QueryCondition.IS, property = "mood", value = "calm"))
            ),
            result = QueryResult(unknown = listOf("mood"))
        )
        setContent {
            NotekeaTheme(darkTheme = false) { QueryContent(state = state, onIntent = {}, saveTarget = null) }
        }

        onNodeWithText(text = "No notes match these filters").assertExists()
        onNodeWithText(text = "No note has the property mood.").assertExists()
    }

    @Test
    fun `a block that is no query names the line, above its YAML`() = runComposeUiTest {
        setContent {
            NotekeaTheme(darkTheme = false) {
                QueryBlock(
                    text = "sorted: rating",
                    source = "```query\nsorted: rating\n```",
                    notesVersion = 0,
                    run = { QueryResult() },
                    onIntent = {},
                    notice = null
                )
            }
        }

        onNodeWithText(text = "This query can't be read").assertExists()
        onNodeWithText(text = "Line 1: a query holds only filters, sort and columns.").assertExists()
    }
}

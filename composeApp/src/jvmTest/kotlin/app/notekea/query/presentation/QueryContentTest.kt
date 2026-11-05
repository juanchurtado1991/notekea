package app.notekea.query.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryResult
import kotlin.test.Test
import kotlin.test.assertEquals

/** The Query tab and its blocks on screen (`ROADMAP.md` 2.10). */
@OptIn(ExperimentalTestApi::class)
class QueryContentTest {
    @Test
    fun `a chip opens its filter, and Apply changes it`() = runComposeUiTest {
        val intents = mutableListOf<QueryIntent>()
        setContent {
            NotekeaTheme(darkTheme = false) {
                QueryContent(state = QueryPreviewData.state, saveTarget = null, onIntent = { intents += it })
            }
        }

        onNodeWithText(text = "rating more than 3").performClick()
        waitForIdle()
        onNode(matcher = hasSetTextAction() and hasText(text = "3")).performTextReplacement(text = "4")
        onNodeWithText(text = "Apply").performClick()
        waitForIdle()

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
        setContent {
            NotekeaTheme(darkTheme = false) {
                QueryContent(state = QueryPreviewData.state, saveTarget = null, onIntent = { intents += it })
            }
        }

        onNodeWithText(text = "rating more than 3").performClick()
        waitForIdle()
        onNode(matcher = hasSetTextAction() and hasText(text = "3")).performTextReplacement(text = "high")
        onNodeWithText(text = "Apply").performClick()
        waitForIdle()

        onNodeWithText(text = "Enter a value of this property's kind.").assertExists()
        assertEquals(expected = emptyList(), actual = intents)
    }

    @Test
    fun `a name opens its note, and the note beside the tab can keep the query`() = runComposeUiTest {
        val intents = mutableListOf<QueryIntent>()
        val note = RelativePath(value = "Books/Reading list.md")
        setContent {
            NotekeaTheme(darkTheme = false) {
                QueryContent(state = QueryPreviewData.state, saveTarget = note, onIntent = { intents += it })
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
            NotekeaTheme(darkTheme = false) { QueryContent(state = state, saveTarget = null, onIntent = {}) }
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
                    notes = emptyList(),
                    notice = null,
                    run = { QueryResult() },
                    onIntent = {}
                )
            }
        }

        onNodeWithText(text = "This query can't be read").assertExists()
        onNodeWithText(text = "Line 1: a query holds only filters, sort and columns.").assertExists()
    }
}

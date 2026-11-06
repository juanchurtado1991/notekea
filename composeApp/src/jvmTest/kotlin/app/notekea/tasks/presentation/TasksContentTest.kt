package app.notekea.tasks.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.tasks.domain.TaskGrouping
import app.notekea.tasks.domain.TaskList
import kotlin.test.Test
import kotlin.test.assertEquals

/** The Tasks tab on screen (`ROADMAP.md` 2.11). */
@OptIn(ExperimentalTestApi::class)
class TasksContentTest {
    @Test
    fun `a task's text opens it, and By date changes the groups`() = runComposeUiTest {
        val intents = mutableListOf<TasksIntent>()
        setContent {
            NotekeaTheme(darkTheme = false) {
                TasksContent(state = TasksPreviewData.state, onIntent = { intents += it })
            }
        }

        onNodeWithText(text = "Write the announcement").performClick()
        onNodeWithText(text = "By date").performClick()
        waitForIdle()

        assertEquals(
            expected = listOf(
                TasksIntent.OnTaskOpen(row = TasksPreviewData.row),
                TasksIntent.OnGroupingChange(grouping = TaskGrouping.DATE)
            ),
            actual = intents
        )
    }

    @Test
    fun `the foot counts, and no task says what to do`() = runComposeUiTest {
        var state by mutableStateOf(value = TasksPreviewData.state)
        setContent {
            NotekeaTheme(darkTheme = false) { TasksContent(state = state, onIntent = {}) }
        }

        onNodeWithText(text = "1 open of 2 tasks").assertExists()
        state = state.copy(list = TaskList())
        waitForIdle()

        onNodeWithText(text = "No open tasks here").assertExists()
    }
}

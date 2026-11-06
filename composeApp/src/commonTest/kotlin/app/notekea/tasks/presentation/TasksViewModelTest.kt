package app.notekea.tasks.presentation

import androidx.lifecycle.viewModelScope
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTask
import app.notekea.core.notes.domain.RelativePath
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tasks.domain.TaskGroup
import app.notekea.tasks.domain.TaskGrouping
import app.notekea.tasks.domain.TaskRow
import app.notekea.tasks.domain.TaskWhen
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.notes.domain.FakeNoteContentStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = FakeNoteContentStore()
    private val viewModel by lazy {
        TasksViewModel(
            vaultPath = VAULT,
            listTasks = ListTasksUseCase(),
            setTaskDone = SetTaskDoneUseCase(store, ApplyLinkUpdatesUseCase(noteContentStore = store)),
            clock = FixedClock,
            localCalendar = FixedCalendar,
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
        viewModel.onIntent(intent = TasksIntent.OnNotesChange(notes = listOf(NOTE)))
        viewModel.onIntent(intent = TasksIntent.OnShownChange(isShown = true))
        advanceUntilIdle()
    }

    private fun rows(): List<TaskRow> = viewModel.state.value.list.groups.flatMap { it.rows }

    @Test
    fun `nothing runs until the tab shows`() = test {
        viewModel.onIntent(intent = TasksIntent.OnNotesChange(notes = listOf(NOTE)))
        advanceUntilIdle()
        val before = viewModel.state.value.isLoading

        show()

        assertTrue(actual = before)
        assertEquals(expected = listOf("Write", "Ship"), actual = rows().map { it.task.text })
    }

    @Test
    fun `grouping by date uses today`() = test {
        show()
        viewModel.onIntent(intent = TasksIntent.OnGroupingChange(grouping = TaskGrouping.DATE))
        advanceUntilIdle()

        val first = viewModel.state.value.list.groups.first()

        assertEquals(expected = TaskWhen.OVERDUE, actual = (first as TaskGroup.OfDate).date)
    }

    @Test
    fun `checking a task writes that one character, tells the editors and leaves the list at once`() = test {
        store.texts[PATH] = TEXT
        show()
        viewModel.onIntent(intent = TasksIntent.OnTaskToggle(row = rows().first()))
        val effect = viewModel.effects.first()
        advanceUntilIdle()

        assertEquals(expected = "- [x] Write\n- [ ] Ship\n", actual = store.texts[PATH])
        assertEquals(expected = TasksEffect.NoteRewritten(path = PATH), actual = effect)
        assertEquals(expected = listOf("Ship"), actual = rows().map { it.task.text })
    }

    @Test
    fun `a second click before the list changes neither undoes the first nor fails`() = test {
        store.texts[PATH] = TEXT
        show()
        val row = rows().first()
        repeat(times = 2) { viewModel.onIntent(intent = TasksIntent.OnTaskToggle(row = row)) }
        advanceUntilIdle()

        assertEquals(expected = "- [x] Write\n- [ ] Ship\n", actual = store.texts[PATH])
        assertEquals(expected = false, actual = viewModel.state.value.isWriteFailed)
    }

    @Test
    fun `another task now at the same place is left alone`() = test {
        store.texts[PATH] = "- [ ] Something much longer\n- [ ] Ship\n"
        show()
        viewModel.onIntent(intent = TasksIntent.OnTaskToggle(row = rows().first()))
        advanceUntilIdle()

        assertEquals(expected = "- [ ] Something much longer\n- [ ] Ship\n", actual = store.texts[PATH])
        assertTrue(actual = viewModel.state.value.isWriteFailed)
    }

    @Test
    fun `a note whose task moved is left alone, and says so`() = test {
        store.texts[PATH] = "New first line\n$TEXT"
        show()
        viewModel.onIntent(intent = TasksIntent.OnTaskToggle(row = rows().first()))
        advanceUntilIdle()

        assertEquals(expected = "New first line\n$TEXT", actual = store.texts[PATH])
        assertTrue(actual = viewModel.state.value.isWriteFailed)
    }

    @Test
    fun `opening a task selects it in its note`() = test {
        show()
        viewModel.onIntent(intent = TasksIntent.OnTaskOpen(row = rows().first()))

        val effect = assertIs<TasksEffect.OpenNote>(value = viewModel.effects.first())

        assertEquals(expected = 6 until 11, actual = effect.range)
    }

    private object FixedClock : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(epochMilliseconds = 0)
    }

    private object FixedCalendar : LocalCalendar {
        override fun momentOf(
            instant: Instant
        ) = LocalMoment(date = TODAY)
    }

    private companion object {
        const val TEXT = "- [ ] Write\n- [ ] Ship\n"
        const val VAULT = "/vault"
        val PATH = RelativePath(value = "Plan.md")
        val TODAY = CalendarDate(year = 2026, month = 11, day = 6)
        val NOTE = NoteEntry(
            file = NoteFile(path = PATH, size = null, modifiedAt = null),
            metadata = NoteMetadata.NONE.copy(
                tasks = listOf(
                    NoteTask(stateIndex = 3, endIndex = 11, text = "Write", isDone = false, date = TODAY.plusDays(-1)),
                    NoteTask(stateIndex = 15, endIndex = 22, text = "Ship", isDone = false)
                )
            )
        )
    }
}

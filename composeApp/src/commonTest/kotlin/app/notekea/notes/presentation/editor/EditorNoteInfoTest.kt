package app.notekea.notes.presentation.editor

import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.viewModelScope
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalMoment
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NoteStats
import app.notekea.notes.domain.NoteTimes
import app.notekea.notes.domain.PropertyValue
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** The side panel's Note info for the open note, counted over real use cases and fakes. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorNoteInfoTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fixture = EditorViewModels(dispatcher = testDispatcher)
    private val viewModel by lazy { fixture.create() }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        fixture.store.texts[NOTE] = TEXT
        fixture.scanner.addNote(file = file(path = NOTE.value))
        fixture.operations.existing += NOTE
        fixture.times.times[NOTE] = NoteTimes(
            createdAt = Instant.parse(input = "2026-10-12T09:30:00Z"),
            modifiedAt = Instant.parse(input = "2026-10-20T18:05:00Z")
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a note's words, links, tags and tasks are counted, its file's dates read, and a save makes it modified now`() =
        runInfoTest {
            send(intent = EditorIntent.OnOpen(path = NOTE))
            val opened = viewModel.state.value.info
            send(
                intent = PropertyIntent.OnValueChange(
                    key = "tags",
                    value = PropertyValue.Items(listOf("recipe", "new"))
                )
            )

            assertEquals(
                expected = NoteInfo(
                    stats = NoteStats(words = 6, links = 1, tags = 2, tasks = 2, doneTasks = 1),
                    createdAt = moment(day = 12, hour = 9, minute = 30),
                    modifiedAt = moment(day = 20, hour = 18, minute = 5)
                ) to NoteInfo(
                    stats = NoteStats(words = 6, links = 1, tags = 3, tasks = 2, doneTasks = 1),
                    createdAt = moment(day = 12, hour = 9, minute = 30),
                    modifiedAt = moment(day = 26, hour = 14, minute = 30, second = 5)
                ),
                actual = opened to viewModel.state.value.info
            )
        }

    private fun moment(
        day: Int,
        hour: Int,
        minute: Int,
        second: Int = 0
    ) = LocalMoment(
        date = CalendarDate(year = 2026, month = 10, day = day),
        hour = hour,
        minute = minute,
        second = second
    )

    private fun TestScope.send(
        intent: EditorIntent
    ) {
        viewModel.onIntent(intent = intent)
        runCurrent()
        Snapshot.sendApplyNotifications()
        advanceTimeBy(delayTimeMillis = SETTLE_MILLIS)
        runCurrent()
    }

    private fun runInfoTest(
        body: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) {
        try {
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    companion object {
        private const val SETTLE_MILLIS = 400L
        private const val TEXT =
            "---\ntags: [recipe]\n---\n# Arepas\n\nSee [[Flour]] #breakfast\n\n- [x] Mix\n- [ ] Rest\n"
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}

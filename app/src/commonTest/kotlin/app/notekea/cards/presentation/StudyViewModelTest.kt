package app.notekea.cards.presentation

import app.notekea.cards.domain.AnswerCardUseCase
import app.notekea.cards.domain.CardAnswer
import app.notekea.cards.domain.ReadDeckUseCase
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/** The Study tab (4.6): the cards due, one at a time, each answer written in its note. */
@OptIn(ExperimentalCoroutinesApi::class)
class StudyViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = FakeNoteContentStore()
    private val note = RelativePath(value = "Capitals.md")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Code review of 2026-11-11: two answers in a row each rewrote the note's old text, and the first was lost.
    @Test
    fun `two cards of one note answered in a row are both scheduled in it`() = runTest(dispatcher) {
        store.texts[note] = "#flashcards\nCapital of Peru::Lima\nCapital of Chile::Santiago"
        store.suspendsLikeDisk = true

        val viewModel = StudyViewModel(
            vaultPath = VAULT,
            readDeck = ReadDeckUseCase(noteContentStore = store),
            answerCard = AnswerCardUseCase(noteContentStore = store),
            localCalendar = FakeLocalCalendar(),
            clock = Clock.System
        )

        val tagged = entry(path = note.value).let {
            it.copy(metadata = it.metadata.copy(textTags = listOf("flashcards")))
        }

        viewModel.onIntent(intent = StudyIntent.OnNotesChange(notes = listOf(tagged)))
        viewModel.onIntent(intent = StudyIntent.OnShownChange(isShown = true))
        advanceUntilIdle()

        repeat(times = 2) {
            viewModel.onIntent(intent = StudyIntent.OnReveal)
            viewModel.onIntent(intent = StudyIntent.OnAnswer(answer = CardAnswer.GOOD))
        }

        advanceUntilIdle()

        assertEquals(
            expected = 2,
            actual = store
                .texts
                .getValue(note)
                .split("<!--SR:")
                .size - 1
        )
    }
}

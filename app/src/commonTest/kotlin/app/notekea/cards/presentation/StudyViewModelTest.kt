package app.notekea.cards.presentation

import app.notekea.cards.data.GhostChoicesReader
import app.notekea.cards.domain.AnswerCardUseCase
import app.notekea.cards.domain.CardAnswer
import app.notekea.cards.domain.MakeChoicesUseCase
import app.notekea.cards.domain.ReadDeckUseCase
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.time.Clock

/** The Study tab (4.6): the cards due, one at a time, each answer written in its note, as multiple choice. */
@OptIn(ExperimentalCoroutinesApi::class)
class StudyViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = FakeNoteContentStore()
    private val note = RelativePath(value = "Capitals.md")
    private val engine = FakeLlmEngine()

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
    fun `two answers in a row are both scheduled, with no model`() = runTest(dispatcher) {
        engine.readiness = Outcome.Failure(error = LlmError.NoModel)
        val viewModel = started()

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

    @Test
    fun `a card is multiple choice, a right choice is Good, a wrong one Hard, and the next card is made ahead`() =
        runTest(dispatcher) {
            engine.reply = { messages ->
                if ("Lima" in messages.last().text) WRONG_PERU else WRONG_CHILE
            }

            val viewModel = started()
            val peru = viewModel.state.value
            val peruChoices = peru.choices!!

            viewModel.onIntent(intent = StudyIntent.OnChoose(index = peruChoices.correct))
            val chosen = viewModel.state.value
            viewModel.onIntent(intent = StudyIntent.OnNext)
            advanceUntilIdle()

            val chile = viewModel.state.value
            val chileChoices = chile.choices!!

            val wrong = chileChoices
                .options
                .indices
                .first {
                    it != chileChoices.correct
                }

            viewModel.onIntent(intent = StudyIntent.OnChoose(index = wrong))
            viewModel.onIntent(intent = StudyIntent.OnNext)
            advanceUntilIdle()

            assertEquals(
                expected = listOf<Any?>(
                    listOf("Bogotá", "Lima", "Quito", "Santiago"),
                    true,
                    2,
                    listOf("250", "230")
                ),
                actual = listOf(
                    peruChoices.options.sorted(),
                    chosen.isShown && chosen.chosen == peruChoices.correct,
                    engine.generated.size,
                    Regex(pattern = ",(\\d+)-->")
                        .findAll(input = store.texts.getValue(note))
                        .map {
                            it.groupValues[1]
                        }
                        .toList()
                )
            )
        }

    private fun TestScope.started(): StudyViewModel {
        store.texts[note] = "#flashcards\nCapital of Peru::Lima\nCapital of Chile::Santiago"
        store.suspendsLikeDisk = true

        val viewModel = StudyViewModel(
            vaultPath = VAULT,
            readDeck = ReadDeckUseCase(noteContentStore = store),
            answerCard = AnswerCardUseCase(noteContentStore = store),
            makeChoices = MakeChoicesUseCase(
                llmEngine = engine,
                reader = GhostChoicesReader()
            ),
            localCalendar = FakeLocalCalendar(),
            clock = Clock.System
        )

        val tagged = entry(path = note.value).let {
            it.copy(metadata = it.metadata.copy(textTags = listOf("flashcards")))
        }

        viewModel.onIntent(intent = StudyIntent.OnNotesChange(notes = listOf(tagged)))
        viewModel.onIntent(intent = StudyIntent.OnShownChange(isShown = true))
        advanceUntilIdle()

        return viewModel
    }

    private companion object {
        const val WRONG_PERU = "{\"wrong\":[\"Quito\",\"Bogotá\",\"lima \",\"Santiago\"]}"
        const val WRONG_CHILE = "{\"wrong\":[\"Buenos Aires\",\"Montevideo\",\"Asunción\"]}"
    }
}

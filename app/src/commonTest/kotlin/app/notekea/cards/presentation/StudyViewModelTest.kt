package app.notekea.cards.presentation

import app.notekea.cards.data.GhostCardsReader
import app.notekea.cards.data.GhostChoicesReader
import app.notekea.cards.domain.AnswerCardUseCase
import app.notekea.cards.domain.CardAnswer
import app.notekea.cards.domain.MakeChoicesUseCase
import app.notekea.cards.domain.ReadDeckUseCase
import app.notekea.cards.domain.ReplaceCardUseCase
import app.notekea.cards.domain.RewriteCardUseCase
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.ViewModelTestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/** The Study tab (4.6): the cards due, one at a time, each answer written in its note, as multiple choice. */
@OptIn(ExperimentalCoroutinesApi::class)
class StudyViewModelTest : ViewModelTestBase() {
    private val store = FakeNoteContentStore()
    private val note = RelativePath(value = "Capitals.md")
    private val engine = FakeLlmEngine()

    // Code review of 2026-11-11: two answers in a row each rewrote the note's old text, and the first was lost.
    @Test
    fun `two answers in a row are both scheduled, with no model`() = runTest(testDispatcher) {
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

    // The code review of 2026-11-22: the effect named the note only, so unsaved edits dropped the card's schedule.
    @Test
    fun `an answer tells the editors the note's text before it, to carry their unsaved edits over`() =
        runTest(testDispatcher) {
            engine.readiness = Outcome.Failure(error = LlmError.NoModel)
            val viewModel = started()
            val effects = mutableListOf<StudyEffect>()

            val collector = launch {
                viewModel.effects.collect { effects += it }
            }

            viewModel.onIntent(intent = StudyIntent.OnReveal)
            viewModel.onIntent(intent = StudyIntent.OnAnswer(answer = CardAnswer.GOOD))
            advanceUntilIdle()
            collector.cancel()

            assertEquals(
                expected = listOf<StudyEffect>(
                    StudyEffect.NoteRewritten(
                        path = note,
                        original = DECK
                    )
                ),
                actual = effects
            )
        }

    @Test
    fun `a card is multiple choice, a right choice is Good, a wrong one Hard, and the next card is made ahead`() =
        runTest(testDispatcher) {
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

    // Plan step 4, 2026-11-26: a hard card's new wording is proposed, and written only when the user chooses it.
    @Test
    fun `a hard card written anew is replaced in its note only when its new wording is chosen`() =
        runTest(testDispatcher) {
            engine.reply = { messages ->
                if ("rewrite" in messages.first().text) REWRITE else WRONG_PERU
            }

            val viewModel = started(deck = HARD_DECK)

            viewModel.onIntent(intent = StudyIntent.OnRewrite)
            advanceUntilIdle()
            val proposed = viewModel.state.value.rewrite?.question
            viewModel.onIntent(intent = StudyIntent.OnKeepCard)
            val kept = store.texts.getValue(note)

            viewModel.onIntent(intent = StudyIntent.OnRewrite)
            advanceUntilIdle()
            viewModel.onIntent(intent = StudyIntent.OnUseRewrite)
            advanceUntilIdle()
            val shown = viewModel.state.value.current

            assertEquals(
                expected = listOf<Any?>(
                    "Which city is Peru's capital?",
                    HARD_DECK,
                    "$SENTENCE\n\n#flashcards\nWhich city is Peru's capital?::Lima <!--source: $SENTENCE-->",
                    "Which city is Peru's capital?",
                    true
                ),
                actual = listOf(proposed, kept, store.texts[note], shown?.question, shown?.isNew)
            )
        }

    private fun TestScope.started(deck: String = DECK): StudyViewModel {
        store.texts[note] = deck
        store.suspendsLikeDisk = true

        val viewModel = StudyViewModel(
            vaultPath = VAULT,
            readDeck = ReadDeckUseCase(noteContentStore = store),
            answerCard = AnswerCardUseCase(noteContentStore = store),
            makeChoices = MakeChoicesUseCase(
                llmEngine = engine,
                reader = GhostChoicesReader()
            ),
            rewriteCard = RewriteCardUseCase(
                llmEngine = engine,
                reader = GhostCardsReader()
            ),
            replaceCard = ReplaceCardUseCase(noteContentStore = store),
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
        const val DECK = "#flashcards\nCapital of Peru::Lima\nCapital of Chile::Santiago"
        const val WRONG_PERU = "{\"wrong\":[\"Quito\",\"Bogotá\",\"lima \",\"Santiago\"]}"
        const val WRONG_CHILE = "{\"wrong\":[\"Buenos Aires\",\"Montevideo\",\"Asunción\"]}"
        const val SENTENCE = "The capital of Peru is Lima."
        const val HARD_DECK =
            "$SENTENCE\n\n#flashcards\nCapital of Peru::Lima <!--source: $SENTENCE--> <!--SR:!2026-11-01,1,170-->"
        const val REWRITE =
            "{\"cards\":[{\"question\":\"Which city is Peru's capital?\",\"answer\":\"Lima\"}]}"
    }
}

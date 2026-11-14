package app.notekea.cards.presentation

import androidx.lifecycle.viewModelScope
import app.notekea.cards.domain.AnswerCardUseCase
import app.notekea.cards.domain.CardAnswer
import app.notekea.cards.domain.CardChoices
import app.notekea.cards.domain.MakeChoicesUseCase
import app.notekea.cards.domain.ReadDeckUseCase
import app.notekea.cards.domain.StudyCard
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

/**
 * The Study tab of the vault at [vaultPath] (4.6): its cards due today, the new ones last, one at a time; an answer
 * schedules the card in its note ([AnswerCardUseCase]) and shows the next. It reads the cards only while it shows.
 * With a chat model each card is asked as a multiple choice question ([MakeChoicesUseCase]), the next one made ahead.
 */
class StudyViewModel(
    private val vaultPath: String,
    private val readDeck: ReadDeckUseCase,
    private val answerCard: AnswerCardUseCase,
    private val makeChoices: MakeChoicesUseCase,
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) : BaseViewModel<StudyState, StudyIntent, StudyEffect>(initialState = StudyState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var notes: Collection<NoteEntry> = emptyList()
    private var isShown = false

    // One answer written at a time: two cards of a note answered quickly would each rewrite the note's old text.
    private val writing = Mutex()

    // Each card's choices, made once by its question and answer: a card read again keeps them.
    private val made = mutableMapOf<Pair<String, String>, Deferred<CardChoices?>>()

    override fun handleIntent(intent: StudyIntent) {
        when (intent) {
            is StudyIntent.OnNotesChange -> {
                notes = intent.notes
                if (isShown) load()
            }

            is StudyIntent.OnShownChange -> {
                isShown = intent.isShown
                if (isShown) load()
            }

            StudyIntent.OnReveal -> reduce {
                it.copy(isShown = it.current != null && it.choices == null)
            }

            is StudyIntent.OnAnswer -> answer(answer = intent.answer)

            is StudyIntent.OnChoose -> choose(index = intent.index)

            StudyIntent.OnNext -> next()

            StudyIntent.OnOpenNote ->
                state
                    .value
                    .current
                    ?.let {
                        emit(
                            effect = StudyEffect.OpenNote(
                                path = it.note,
                                line = it.line
                            )
                        )
                    }
        }
    }

    private fun load() {
        launchGuarded(onError = ::logError) {
            val today = today()

            // After an answer being written, never beside it: read a moment before, the deck still had the card
            // as due and it came back to be answered again (the code review of 2026-11-13).
            val cards = writing.withLock {
                readDeck(
                    vaultPath = vaultPath,
                    notes = notes
                )
            }

            val due = cards.filter {
                it.isDue(today = today)
            }.sortedWith(compareBy({ it.isNew }, { it.due }))

            reduce {
                // The card on screen stays as it is (its answer, its choice) when only another card changed.
                val shown = it.current?.let(::keyOf)
                val isSame = shown != null && due.firstOrNull()?.let(::keyOf) == shown

                it.copy(
                    isLoading = false,
                    due = due,
                    isShown = isSame && it.isShown,
                    choices = if (isSame) it.choices else null,
                    chosen = if (isSame) it.chosen else null,
                    total = cards.size,
                    nextDate = cards
                        .mapNotNull { card -> card.due }
                        .filter { date ->
                            date > today
                        }
                        .minOrNull()
                )
            }

            askCurrent()
        }
    }

    // The current card's choices, made now if they are not yet, and the next card's made meanwhile.
    private fun askCurrent() {
        val current = state.value
        val card = current.current ?: return
        if (current.choices != null || current.isShown) return

        reduce { it.copy(isMakingChoices = true) }

        launchGuarded(onError = ::logError) {
            val choices = try {
                choicesOf(card = card).await()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                logError(throwable = exception)
                null
            }

            // Choices that could not be made (the model was not ready) are asked again next time, not kept as none.
            if (choices == null) made.remove(key = keyOf(card = card))

            reduce {
                val isStill = it.current?.let(::keyOf) == keyOf(card = card) && !it.isShown

                it.copy(
                    isMakingChoices = false,
                    choices = if (isStill) choices else it.choices
                )
            }

            state
                .value
                .due
                .getOrNull(index = 1)
                ?.let { choicesOf(card = it) }
        }
    }

    private fun choicesOf(card: StudyCard): Deferred<CardChoices?> = made
        .getOrPut(key = keyOf(card = card)) {
            viewModelScope.async { makeChoices(card = card) }
        }

    private fun choose(index: Int) {
        reduce {
            val choices = it.choices

            if (choices == null ||
                it.chosen != null ||
                index !in choices.options.indices
            ) {
                it
            } else {
                it.copy(
                    chosen = index,
                    isShown = true
                )
            }
        }
    }

    private fun next() {
        val current = state.value
        val chosen = current.chosen ?: return
        val isCorrect = current.choices?.isCorrect(chosen = chosen) == true

        answer(answer = if (isCorrect) CardAnswer.GOOD else CardAnswer.HARD)
    }

    private fun answer(answer: CardAnswer) {
        val card = state.value.current ?: return
        val isAnswerShown = state.value.isShown
        if (!isAnswerShown) return

        made.remove(key = keyOf(card = card))

        reduce {
            it.copy(
                due = it.due.drop(n = 1),
                isShown = false,
                choices = null,
                chosen = null,
                reviewed = it.reviewed + 1
            )
        }

        askCurrent()

        launchGuarded(onError = ::logError) {
            val scheduled = writing.withLock {
                answerCard(
                    vaultPath = vaultPath,
                    card = card,
                    answer = answer,
                    today = today()
                )
            }

            if (scheduled != null) emit(effect = StudyEffect.NoteRewritten(path = card.note))

            reduce { current ->
                val next = listOfNotNull(current.nextDate, scheduled?.due).minOrNull()
                current.copy(nextDate = next)
            }
        }
    }

    // A card is the same card while its question and answer are, whatever its schedule says.
    private fun keyOf(card: StudyCard): Pair<String, String> = card.question to card.answer

    private fun today(): CalendarDate = localCalendar.momentOf(instant = clock.now()).date

    private fun logError(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "StudyViewModel"
        const val FAILED_MESSAGE = "The study cards could not be read or scheduled"
    }
}

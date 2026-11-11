package app.notekea.cards.presentation

import app.notekea.cards.domain.AnswerCardUseCase
import app.notekea.cards.domain.CardAnswer
import app.notekea.cards.domain.ReadDeckUseCase
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger
import kotlin.time.Clock

/**
 * The Study tab of the vault at [vaultPath] (4.6): its cards due today, the new ones last, one at a time; an answer
 * schedules the card in its note ([AnswerCardUseCase]) and shows the next. It reads the cards only while it shows.
 */
class StudyViewModel(
    private val vaultPath: String,
    private val readDeck: ReadDeckUseCase,
    private val answerCard: AnswerCardUseCase,
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) : BaseViewModel<StudyState, StudyIntent, StudyEffect>(initialState = StudyState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var notes: Collection<NoteEntry> = emptyList()
    private var isShown = false

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

            StudyIntent.OnReveal -> reduce { it.copy(isShown = it.current != null) }

            is StudyIntent.OnAnswer -> answer(answer = intent.answer)

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

            val cards = readDeck(
                vaultPath = vaultPath,
                notes = notes
            )

            val due = cards.filter {
                it.isDue(today = today)
            }.sortedWith(compareBy({ it.isNew }, { it.due }))

            reduce {
                it.copy(
                    isLoading = false,
                    due = due,
                    isShown = false,
                    total = cards.size,
                    nextDate = cards
                        .mapNotNull { card -> card.due }
                        .filter { date ->
                            date > today
                        }
                        .minOrNull()
                )
            }
        }
    }

    private fun answer(answer: CardAnswer) {
        val card = state.value.current ?: return
        val isAnswerShown = state.value.isShown
        if (!isAnswerShown) return

        reduce {
            it.copy(
                due = it.due.drop(n = 1),
                isShown = false,
                reviewed = it.reviewed + 1
            )
        }

        launchGuarded(onError = ::logError) {
            val scheduled = answerCard(
                vaultPath = vaultPath,
                card = card,
                answer = answer,
                today = today()
            )

            if (scheduled != null) emit(effect = StudyEffect.NoteRewritten(path = card.note))

            reduce { current ->
                val next = listOfNotNull(current.nextDate, scheduled?.due).minOrNull()
                current.copy(nextDate = next)
            }
        }
    }

    private fun today(): CalendarDate = localCalendar.momentOf(instant = clock.now()).date

    private fun logError(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "StudyViewModel"
        const val FAILED_MESSAGE = "The study cards could not be read or scheduled"
    }
}

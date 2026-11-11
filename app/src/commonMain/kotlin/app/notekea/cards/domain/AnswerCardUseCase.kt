package app.notekea.cards.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NoteContentStore

/**
 * Records how well [StudyCard] was remembered (4.6): its next date written in its note as the Spaced Repetition plugin
 * writes it. The card is looked for again by its question when its note changed since it was read; a card gone from
 * its note is only skipped.
 */
class AnswerCardUseCase(private val noteContentStore: NoteContentStore) {
    suspend operator fun invoke(
        vaultPath: String,
        card: StudyCard,
        answer: CardAnswer,
        today: CalendarDate
    ): StudyCard? {
        val text = noteContentStore.read(
            vaultPath = vaultPath,
            path = card.note
        ) ?: return null

        val current = CardFormat.cardsOf(
            note = card.note,
            text = text
        )

        val found = current.firstOrNull {
            it.line == card.line && it.question == card.question
        }
            ?: current.firstOrNull {
                it.question == card.question
            }
            ?: return null

        val scheduled = CardSchedule.after(
            card = found,
            answer = answer,
            today = today
        )

        val written = noteContentStore.write(
            vaultPath = vaultPath,
            path = card.note,
            content = CardFormat.scheduled(
                text = text,
                card = scheduled
            )
        )

        return if (written is Outcome.Success) scheduled else null
    }
}

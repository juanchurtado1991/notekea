package app.notekea.cards.presentation

import app.notekea.cards.domain.CardAnswer
import app.notekea.core.notes.domain.NoteEntry

sealed interface StudyIntent {
    /** The vault's notes changed: its cards are read again while the tab shows. */
    data class OnNotesChange(val notes: Collection<NoteEntry>) : StudyIntent

    data class OnShownChange(val isShown: Boolean) : StudyIntent

    /** Show answer (Space). */
    data object OnReveal : StudyIntent

    /** Hard, Good or Easy (1, 2, 3): the card is scheduled in its note and the next one shows. */
    data class OnAnswer(val answer: CardAnswer) : StudyIntent

    /** The card's note opens at its line. */
    data object OnOpenNote : StudyIntent
}

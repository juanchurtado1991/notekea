package app.notekea.cards.presentation

import app.notekea.cards.domain.CardAnswer
import app.notekea.core.notes.domain.NoteEntry

sealed interface StudyIntent {
    /** The vault's notes changed: its cards are read again while the tab shows. */
    data class OnNotesChange(val notes: Collection<NoteEntry>) : StudyIntent

    data class OnShownChange(val isShown: Boolean) : StudyIntent

    /** Show answer (Space), when the card has no choices. */
    data object OnReveal : StudyIntent

    /** Hard, Good or Easy (1, 2, 3): the card is scheduled in its note and the next one shows. */
    data class OnAnswer(val answer: CardAnswer) : StudyIntent

    /** The option at [index] chosen (1 to 4): it is marked right or wrong and the right one shows. */
    data class OnChoose(val index: Int) : StudyIntent

    /** Next (Enter) after choosing: a right choice schedules the card as Good, a wrong one as Hard. */
    data object OnNext : StudyIntent

    /** The hard card shown is written anew from its sentence: proposed, never written alone. */
    data object OnRewrite : StudyIntent

    /** The new wording replaces the card in its note, where it starts again as a new card. */
    data object OnUseRewrite : StudyIntent

    /** The card stays as it was, and the new wording is dropped. */
    data object OnKeepCard : StudyIntent

    /** The card's note opens at its line. */
    data object OnOpenNote : StudyIntent
}

package app.notekea.shell.data.chat

import app.notekea.cards.domain.CardFormat
import app.notekea.cards.domain.CardsProblem
import app.notekea.cards.domain.MakeCardsUseCase
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.CardMaker
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath

/** The chat's [CardMaker], made of the cards feature (4.6): the cards the note backs, as the block added to it. */
class CardsForChat(private val makeCards: MakeCardsUseCase) : CardMaker {
    override suspend fun cardsFor(
        vaultPath: String,
        note: RelativePath
    ): Outcome<String, ActionProblem> =
        when (val made = makeCards(vaultPath = vaultPath, note = note)) {
            is Outcome.Success -> Outcome.Success(
                value = CardFormat.blockOf(
                    cards = made.value.map {
                        it.question to it.answer
                    }
                )
            )

            is Outcome.Failure -> Outcome.Failure(
                error = problemOf(
                    problem = made.error,
                    note = note
                )
            )
        }

    private fun problemOf(
        problem: CardsProblem,
        note: RelativePath
    ): ActionProblem = when (problem) {
        CardsProblem.NOTE_UNREADABLE -> ActionProblem.UnknownNote(name = note.nameWithoutExtension)
        CardsProblem.MODEL_FAILED, CardsProblem.NONE_BACKED -> ActionProblem.NoCards
    }
}

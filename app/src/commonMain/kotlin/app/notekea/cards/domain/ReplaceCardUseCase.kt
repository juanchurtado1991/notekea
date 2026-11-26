package app.notekea.cards.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteContentStore

/**
 * A hard card replaced in its note by the wording the user chose ([RewriteCardUseCase]): it starts again as a new
 * card. The card is looked for again by its question when its note changed since it was read; one gone is skipped.
 */
class ReplaceCardUseCase(private val noteContentStore: NoteContentStore) {
    suspend operator fun invoke(
        vaultPath: String,
        card: StudyCard,
        with: ProposedCard
    ): AnsweredCard? {
        val text = noteContentStore.read(
            vaultPath = vaultPath,
            path = card.note
        ) ?: return null

        val found = CardFormat.located(
            text = text,
            card = card
        ) ?: return null

        val content = CardFormat.replaced(
            text = text,
            card = found,
            with = with
        )

        val written = noteContentStore.write(
            vaultPath = vaultPath,
            path = card.note,
            content = content
        )

        if (written is Outcome.Failure) return null

        val replaced = CardFormat
            .cardsOf(
                note = card.note,
                text = content
            )
            .firstOrNull {
                it.line == found.line
            }

        return replaced?.let {
            AnsweredCard(
                card = it,
                original = text
            )
        }
    }
}

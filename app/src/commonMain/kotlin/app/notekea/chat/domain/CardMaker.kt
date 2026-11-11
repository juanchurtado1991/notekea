package app.notekea.chat.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath

/** Makes study cards from a note (4.6), for the chat: the block to add to it; the shell gives it the cards feature. */
fun interface CardMaker {
    suspend fun cardsFor(
        vaultPath: String,
        note: RelativePath
    ): Outcome<String, ActionProblem>
}

package app.notekea.chat.presentation.model

import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.answer.AnswerCitations

/** This answer stopped where it was: its text so far stays, with the citations it already holds. */
internal fun ChatEntry.stopped(): ChatEntry {
    val kept = AnswerCitations.of(
        text = answer,
        read = sources
    )

    return copy(
        step = ChatStep.DONE,
        answer = kept.text,
        sources = kept.sources,
        outcome = TurnOutcome.STOPPED
    )
}

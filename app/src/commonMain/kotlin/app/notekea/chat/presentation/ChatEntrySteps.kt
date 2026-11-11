package app.notekea.chat.presentation

import app.notekea.chat.domain.AnswerStep
import app.notekea.chat.domain.TurnOutcome

/** The entry once its answer reached [step]: where it stands, the text so far, how it ended. */
internal fun ChatEntry.after(step: AnswerStep): ChatEntry = when (step) {
    AnswerStep.Searching -> copy(step = ChatStep.SEARCHING)

    is AnswerStep.Reading -> copy(
        step = ChatStep.READING,
        readNotes = step.notes
    )

    is AnswerStep.Writing -> copy(
        step = ChatStep.WRITING,
        answer = step.text,
        sources = step.read
    )

    is AnswerStep.Drafting -> copy(step = ChatStep.DRAFTING)

    is AnswerStep.Offered -> copy(offered = step.request)

    is AnswerStep.Done -> copy(
        step = ChatStep.DONE,
        answer = step.answer.text,
        sources = step.answer.sources,
        outcome = TurnOutcome.ANSWERED
    )

    AnswerStep.NotFound -> copy(
        step = ChatStep.DONE,
        answer = "",
        sources = emptyList(),
        outcome = TurnOutcome.NOT_FOUND
    )

    is AnswerStep.Failed -> copy(
        step = ChatStep.DONE,
        outcome = TurnOutcome.FAILED,
        error = step.error
    )
}

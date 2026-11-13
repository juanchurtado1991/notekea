package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.chat.presentation.model.PassedStep

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

/**
 * The entry after [change], remembering the step it left when it moved on to another, so the chat lists what was done
 * with a check and what is going on under it (the developer's smoke of 2026-11-13).
 */
internal fun ChatEntry.changedBy(change: (ChatEntry) -> ChatEntry): ChatEntry {
    val changed = change(this)
    val isSameStep = changed.step == step || step == ChatStep.DONE
    if (isSameStep) return changed

    val left = PassedStep(
        step = step,
        readNotes = readNotes
    )

    return changed.copy(passed = changed.passed + left)
}

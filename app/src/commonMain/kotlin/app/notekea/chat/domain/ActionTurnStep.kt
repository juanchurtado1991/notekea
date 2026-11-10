package app.notekea.chat.domain

/** Where a request stands while the chat acts on it. */
sealed interface ActionTurnStep {
    /** The model is choosing what to do. */
    data object Planning : ActionTurnStep

    /** It is a question: the chat answers it from the notes ([AnswerFromNotesUseCase]). */
    data object Answer : ActionTurnStep

    /** Notes or tasks a step found, shown as a table. */
    data class Found(
        val step: ActionStep,
        val found: FoundNotes
    ) : ActionTurnStep

    /** Steps that only show something (a note, a panel, the theme): the app does them now. */
    data class Shown(val steps: List<ActionStep>) : ActionTurnStep

    /** The text of the notes to write is being written. */
    data object Writing : ActionTurnStep

    /** Steps that change the vault, written out, waiting for the card's Do it. */
    data class Proposed(val steps: List<ActionStep>) : ActionTurnStep

    data class Problem(val problem: ActionProblem) : ActionTurnStep

    /** Nothing waits for a confirmation. */
    data object Finished : ActionTurnStep
}

package app.notekea.chat.domain.plan

import app.notekea.core.notes.domain.RelativePath

/** Where a request stands while the chat acts on it. */
sealed interface ActionTurnStep {
    /** The model is choosing what to do. */
    data object Planning : ActionTurnStep

    /**
     * It is a question: the chat answers it from the notes (`AnswerFromNotesUseCase`), searching for [question], the
     * request as it stands alone once the chat so far says what "it" or "that" was.
     */
    data class Answer(val question: String) : ActionTurnStep

    /** Not about the notes' facts (a greeting, an opinion): the chat talks, reading the [note] it is about, if any. */
    data class Chat(val note: RelativePath?) : ActionTurnStep

    /** How to use Notekea: explained from the guide, the plan's [request] being the question as it stands alone. */
    data class Help(val request: String) : ActionTurnStep

    /** What the vault holds as a whole, described from its map. */
    data object Overview : ActionTurnStep

    /**
     * Agent mode works on it (`AgentUse`): asked for, or the plan named what only a look-up finds. [planned] is the
     * plan it was handed with: what the request asks to write, proposed if the agent ends without a proposal.
     */
    data class ToAgent(val planned: List<PlannedStep> = emptyList()) : ActionTurnStep

    /** Notes or tasks a step found, shown as a table. */
    data class Found(
        val step: ActionStep,
        val found: FoundNotes
    ) : ActionTurnStep

    /** Steps that only show something (a note, a panel, the theme): the app does them now. */
    data class Shown(val steps: List<ActionStep>) : ActionTurnStep

    /** The text of the notes to write is being written. */
    data object Writing : ActionTurnStep

    /** A clip of many pages is under way: how far it is. */
    data class Progress(val progress: PageProgress) : ActionTurnStep

    /** Steps that change the vault, written out, waiting for the card's Do it. */
    data class Proposed(val steps: List<ActionStep>) : ActionTurnStep

    data class Problem(val problem: ActionProblem) : ActionTurnStep

    /** Nothing waits for a confirmation. */
    data object Finished : ActionTurnStep
}

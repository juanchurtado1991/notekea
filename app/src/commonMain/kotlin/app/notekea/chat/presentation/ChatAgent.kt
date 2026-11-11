package app.notekea.chat.presentation

import app.notekea.chat.domain.ActionProblem
import app.notekea.chat.domain.AgentStep
import app.notekea.chat.domain.AgentTaskUseCase
import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.SpokenLanguage
import app.notekea.chat.domain.TurnOutcome
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * Agent mode in the chat (4.7), for [ChatViewModel]: a request worked on move after move, each move listed on its
 * entry, and its end shown as the usual card (its plan, confirmed before anything changes) or as what it found.
 */
internal class ChatAgent(
    private val vaultPath: String,
    private val agentTask: AgentTaskUseCase,
    private val actions: ChatActions,
    private val update: (id: Long, change: (ChatEntry) -> ChatEntry) -> Unit
) {
    /** The entry handed to the agent, its end to come: asked of agent mode, or a question Auto could not answer. */
    suspend fun takeOver(
        id: Long,
        task: String,
        notes: Collection<NoteEntry>,
        history: List<ChatExchange>,
        openNote: RelativePath?
    ) {
        update(id) {
            it.copy(
                answeredWith = ChatMode.AGENT,
                outcome = null
            )
        }

        work(id, task, notes, history, openNote)
    }

    private suspend fun work(
        id: Long,
        task: String,
        notes: Collection<NoteEntry>,
        history: List<ChatExchange>,
        openNote: RelativePath?
    ) {
        update(id) { it.copy(step = ChatStep.WORKING) }

        val context = actions.contextOf(
            request = task,
            notes = notes,
            openNote = openNote
        ).copy(
            history = history,
            language = SpokenLanguage.inChat(
                text = task,
                history = history
            )
        )

        agentTask(
            task = task,
            vaultPath = vaultPath,
            notes = notes,
            context = context,
            openNote = openNote
        )
            .collect { step ->
                update(id) { entry -> entry.after(step = step) }
            }
    }

    private fun ChatEntry.after(step: AgentStep): ChatEntry = when (step) {
        is AgentStep.Moving -> copy(
            moves = moves + AgentMoveLine(
                move = step.move,
                target = step.target
            )
        )

        is AgentStep.Proposed -> copy(
            step = ChatStep.DONE,
            answer = step.summary,
            outcome = TurnOutcome.ACTED,
            action = ChatAction(
                status = ActionStatus.PROPOSED,
                steps = step.steps
            )
        )

        is AgentStep.Finished -> copy(
            step = ChatStep.DONE,
            answer = step.summary,
            outcome = TurnOutcome.ANSWERED
        )

        is AgentStep.Stopped -> copy(
            step = ChatStep.DONE,
            outcome = TurnOutcome.ACTED,
            action = ChatAction(
                status = ActionStatus.FAILED,
                problem = step.problem ?: ActionProblem.AgentStopped
            )
        )
    }
}

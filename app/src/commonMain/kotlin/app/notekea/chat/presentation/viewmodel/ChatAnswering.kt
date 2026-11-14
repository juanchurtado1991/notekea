package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.agent.AgentUse
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatMode
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * A question answered for [ChatViewModel]: planned in the whole vault (an action, talk, help) or answered from a note
 * or a folder, written step by step, and handed to the agent when it asks for it or when Auto found no answer.
 */
internal class ChatAnswering(
    private val memory: ChatMemory,
    private val actions: ChatActions,
    private val agent: ChatAgent,
    private val replies: ChatReplies,
    private val host: ChatHost
) {
    suspend fun answer(
        id: Long,
        question: String,
        scope: ChatScope
    ) {
        val history = memory.historyBefore(
            entries = host.state.value.entries,
            id = id
        )

        val current = host.state.value

        // A request in the whole vault may be an action or talk; one asked of a note or a folder is answered.
        val reply = if (scope == ChatScope.VAULT) {
            actions.act(
                id = id,
                request = question,
                notes = host.notes,
                history = history,
                openNote = host.activeNote,
                agentUse = current.mode.agentUse(isAgentOffered = current.isAgentOffered)
            )
        } else {
            ChatReply.Question(question = question)
        }

        // The agent ends on a card or with what it found, never in an answer of 3.2.
        if (reply is ChatReply.Agent) {
            return agent.takeOver(id, question, host.notes, history, host.activeNote, reply.planned)
        }

        if (reply == ChatReply.Acted) return

        written(
            id = id,
            question = question,
            scope = scope,
            reply = reply,
            history = history
        )

        // Auto does what it can: a question the notes' passages did not answer is worked on by the agent.
        val isAgentNext =
            host.state.value.entries.lastOrNull { it.id == id }?.outcome == TurnOutcome.NOT_FOUND &&
                scope == ChatScope.VAULT &&
                current.mode.agentUse(current.isAgentOffered) == AgentUse.WHEN_STUCK

        if (isAgentNext) agent.takeOver(id, question, host.notes, history, host.activeNote)
    }

    private suspend fun written(
        id: Long,
        question: String,
        scope: ChatScope,
        reply: ChatReply,
        history: List<ChatExchange>
    ) {
        val isDeep = host
            .state
            .value
            .isDeepFor(reply = reply)

        // Tables found are answered in words while the turn is still answering: its end was the action's
        // (the chat probe, 2026-11-11: the reply was cut at its first words).
        host
            .update(id) {
                it.copy(
                    answeredWith = if (isDeep) ChatMode.DEEP else ChatMode.FAST,
                    outcome = if (reply is ChatReply.FromFound) null else it.outcome
                )
            }

        replies
            .stepsFor(
                reply = reply,
                message = question,
                history = history,
                notes = host.notes,
                only = notesOf(scope, host.notes, host.activeNote),
                isDeep = isDeep,
                onStep = { step ->
                    host.update(id) { it.copy(step = step) }
                }
            )
            .collect { step -> host.follow(id, step) }
    }
}

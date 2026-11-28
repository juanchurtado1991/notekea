package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.ChatScope

/**
 * A question answered for [ChatViewModel]: worked on by the agent's loop, of the whole vault or of the open note or
 * its folder; talk, help and the overview the loop hands back are written by the chat's own replies.
 */
internal class ChatAnswering(
    private val keeping: ChatKeeping,
    private val agent: ChatAgent,
    private val replies: ChatReplies,
    private val host: ChatHost
) {
    suspend fun answer(
        id: Long,
        question: String,
        scope: ChatScope
    ) {
        val history = keeping.historyBefore(id = id)

        val reply = agent.takeOver(
            id = id,
            task = question,
            notes = host.notes,
            history = history,
            openNote = host.activeNote,
            scope = notesOf(scope, host.notes, host.activeNote)
        ) ?: return

        replies
            .stepsFor(
                reply = reply,
                message = question,
                history = history,
                notes = host.notes
            )
            .collect { step -> host.follow(id, step) }
    }
}

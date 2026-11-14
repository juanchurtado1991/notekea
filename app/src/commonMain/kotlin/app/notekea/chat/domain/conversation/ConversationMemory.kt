package app.notekea.chat.domain.conversation

import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.core.domain.Outcome

/**
 * What the model is given of a [Conversation], as Claude Code's context: the summary of what came before and the
 * last few exchanges after it, each with what the chat did ([TurnRecord]). It fills as the chat goes on ([usageOf])
 * and, once full, is compacted: all but the last exchanges become the summary.
 */
class ConversationMemory(private val compactChat: CompactChatUseCase) {
    /**
     * What a question asked after [conversation] is given of it: the summary, the chat's first exchange (its start,
     * for "going back to the start…", the chat probe of 2026-11-11), then the last exchanges.
     */
    fun historyOf(conversation: Conversation): List<ChatExchange> {
        val earlier = conversation.summary?.let {
            ChatExchange(
                question = EARLIER,
                reply = it
            )
        }

        val before = remembered(conversation = conversation)
        val recent = before.takeLast(n = TURNS)
        val start = before.take(n = (before.size - TURNS).coerceIn(0, 1))

        return listOfNotNull(earlier) + (start + recent).map { exchangeOf(turn = it) }
    }

    /** How full the memory is, from 0 to 1 and over: the summary and every exchange since, against its budget. */
    fun usageOf(conversation: Conversation): Float {
        val used = conversation
            .summary
            .orEmpty()
            .length + remembered(conversation = conversation).sumOf { exchangeOf(turn = it).size }

        return used.toFloat() / BUDGET_CHARS
    }

    /**
     * [conversation] with all but its last exchanges written into the summary; itself when there is nothing to
     * compact or nothing shorter to say. Failed, the older exchanges are forgotten all the same, so the memory
     * empties and is not compacted again after every reply.
     */
    suspend fun compact(conversation: Conversation): Conversation {
        val older = remembered(conversation = conversation).dropLast(n = KEPT)
        if (older.isEmpty()) return conversation

        val compacted = compactChat(
            exchanges = older.map { exchangeOf(turn = it) },
            summary = conversation.summary
        )

        val isWritten = compacted is Outcome.Success && compacted.value.isNotBlank()

        val replaced = conversation
            .summary
            .orEmpty()
            .length + older.sumOf { exchangeOf(turn = it).size }

        val shorter = if (isWritten) {
            within(
                text = compacted.value,
                chars = replaced
            )
        } else {
            null
        }

        val isNoShorter = isWritten && shorter == null
        if (isNoShorter) return conversation

        return conversation.copy(
            summary = shorter ?: conversation.summary,
            compacted = conversation.turns.indexOfLast {
                it === older.last()
            } + 1
        )
    }

    // A summary never fills the memory more than what it replaces: compacting a short chat raised the ring (the
    // Auto-Test on the real app, 2026-11-11). It is cut at a sentence when longer; null when no sentence fits.
    private fun within(
        text: String,
        chars: Int
    ): String? {
        if (text.length <= chars) return text

        val cut = text.take(n = chars)
        val end = cut.lastIndexOfAny(chars = SENTENCE_ENDS)

        return if (end > 0) cut.take(n = end + 1) else null
    }

    // An answer, or what the chat did for a request: asked later why it wrote something, it knows what it did.
    private fun remembered(conversation: Conversation): List<ChatTurn> = conversation
        .turns
        .drop(n = conversation.compacted)
        .filter { repliedOf(turn = it).isNotBlank() }

    // What it said, and under it what it did: an agent's proposal has a summary of its own, which alone would
    // leave out what it read and how the note's text was written (code review, 2026-11-14).
    private fun repliedOf(turn: ChatTurn): String {
        val said = turn.answer.take(n = REPLY_CHARS)

        val did = TurnRecord
            .of(turn = turn)
            ?.take(n = RECORD_CHARS)
            .orEmpty()

        return listOf(said, did).filter { it.isNotBlank() }.joinToString(separator = "\n")
    }

    private fun exchangeOf(turn: ChatTurn) = ChatExchange(
        question = turn.question,
        reply = repliedOf(turn = turn)
    )

    private val ChatExchange.size: Int get() = question.length + reply.length

    private companion object {
        const val TURNS = 4
        val SENTENCE_ENDS = charArrayOf('.', '!', '?')
        const val KEPT = 2
        const val REPLY_CHARS = 400
        const val RECORD_CHARS = 900
        const val BUDGET_CHARS = 6_000
        const val EARLIER = "(Earlier in this chat)"
    }
}

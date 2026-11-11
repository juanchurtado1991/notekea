package app.notekea.chat.presentation

import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.core.domain.Outcome

/**
 * What the chat remembers of itself for the model, as Claude Code's context: a [summary] of what came before and the
 * last few exchanges after it. It fills as the chat goes on ([usageOf]) and, once full, is compacted: all but the last
 * exchanges become the summary.
 */
internal class ChatMemory(private val compactChat: CompactChatUseCase) {
    var summary: String? = null
        private set

    private var compactedThrough = NONE

    /** What a question asked as entry [id] is given of the chat before it: the summary, then the last exchanges. */
    fun historyBefore(
        entries: List<ChatEntry>,
        id: Long
    ): List<ChatExchange> {
        val earlier = summary?.let {
            ChatExchange(
                question = EARLIER,
                reply = it
            )
        }

        val recent = remembered(entries = entries)
            .filter {
                it.id < id
            }
            .takeLast(n = TURNS)
            .map { it.exchange() }

        return listOfNotNull(earlier) + recent
    }

    /** How full the memory is, from 0 to 1 and over: the summary and every exchange since, against its budget. */
    fun usageOf(entries: List<ChatEntry>): Float {
        val used = summary.orEmpty().length + remembered(entries = entries).sumOf {
            it.exchange().size
        }

        return used.toFloat() / BUDGET_CHARS
    }

    /** All but the last exchanges written into the summary; false when there is nothing to compact or it failed. */
    suspend fun compact(entries: List<ChatEntry>): Boolean {
        val older = remembered(entries = entries).dropLast(n = KEPT)
        if (older.isEmpty()) return false

        val compacted = compactChat(
            exchanges = older.map { it.exchange() },
            summary = summary
        )

        if (compacted !is Outcome.Success || compacted.value.isBlank()) return false

        summary = compacted.value
        compactedThrough = older.last().id

        return true
    }

    fun clear() {
        summary = null
        compactedThrough = NONE
    }

    private fun remembered(entries: List<ChatEntry>): List<ChatEntry> = entries.filter {
        it.id > compactedThrough && it.answer.isNotBlank()
    }

    private fun ChatEntry.exchange() = ChatExchange(
        question = question,
        reply = answer.take(n = REPLY_CHARS)
    )

    private val ChatExchange.size: Int get() = question.length + reply.length

    private companion object {
        const val NONE = -1L
        const val TURNS = 4
        const val KEPT = 2
        const val REPLY_CHARS = 400
        const val BUDGET_CHARS = 6_000
        const val EARLIER = "(Earlier in this chat)"
    }
}

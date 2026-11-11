package app.notekea.chat.presentation

import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.core.domain.Outcome
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * What the chat remembers of itself for the model, as Claude Code's context: a [summary] of what came before and the
 * last few exchanges after it. It fills as the chat goes on ([usageOf]) and, once full, is compacted: all but the last
 * exchanges become the summary.
 */
internal class ChatMemory(private val compactChat: CompactChatUseCase) {
    var summary: String? = null
        private set

    private var compactedThrough = NONE

    // When the last question was asked: a chat left for a while, or one from an earlier session, is compacted first.
    private var lastAsked: Instant? = null

    /** The chat loaded from an earlier session: its next question compacts it first. */
    fun resume() {
        lastAsked = Instant.DISTANT_PAST
    }

    /** Whether a question asked [now] comes [IDLE] or more after the last one; it becomes the last one. */
    fun isIdleAt(now: Instant): Boolean {
        val isIdle = lastAsked?.let {
            now - it >= IDLE
        } == true

        lastAsked = now

        return isIdle
    }

    /**
     * What a question asked as entry [id] is given of the chat before it: the summary, the chat's first exchange (its
     * start, for "going back to the start…", the chat probe of 2026-11-11), then the last exchanges.
     */
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

        val before = remembered(entries = entries).filter {
            it.id < id
        }

        val recent = before.takeLast(n = TURNS)

        val start = before.firstOrNull()?.takeIf {
            it !in recent
        }

        return listOfNotNull(earlier, start?.exchange()) + recent.map { it.exchange() }
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

        val isWritten = compacted is Outcome.Success && compacted.value.isNotBlank()

        val replaced = summary.orEmpty().length + older.sumOf { it.exchange().size }

        val shorter = if (isWritten) {
            within(
                text = compacted.value,
                chars = replaced
            )
        } else {
            null
        }

        // Nothing shorter to say than what is there: the chat stays as it is.
        if (isWritten && shorter == null) return false

        // Failed, the older exchanges are forgotten all the same, so the memory empties and is not compacted again
        // after every reply.
        if (shorter != null) summary = shorter
        compactedThrough = older.last().id

        return isWritten
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
        val SENTENCE_ENDS = charArrayOf('.', '!', '?')
        val IDLE = 30.minutes
        const val KEPT = 2
        const val REPLY_CHARS = 400
        const val BUDGET_CHARS = 6_000
        const val EARLIER = "(Earlier in this chat)"
    }
}

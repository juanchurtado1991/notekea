package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.ChatHistoryStore
import app.notekea.chat.domain.conversation.Conversation
import app.notekea.chat.domain.conversation.ConversationMemory
import app.notekea.chat.presentation.model.ChatEntry
import kotlinx.coroutines.Job
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * What the chat keeps: its conversation in the vault's history ([historyStore], the last [MAX_TURNS] turns, from the
 * moment each is asked) and what the model is given of it ([memory]), which the last answer fills and which is
 * compacted by itself once full, when asked, or before a question asked after a while, as a new session starts.
 */
internal class ChatKeeping(
    private val vaultPath: String,
    private val historyStore: ChatHistoryStore,
    private val memory: ConversationMemory,
    private val clock: Clock,
    private val host: ChatHost
) {
    private var compaction: Job? = null
    private var summary: String? = null
    private var compactedThrough = NONE

    // When the last question was asked: a chat left for a while, or one from an earlier session, is compacted first.
    private var lastAsked: Instant? = null

    /** The chat of an earlier session as entries, numbered by [nextId]; its next question compacts it first. */
    suspend fun load(nextId: () -> Long): List<ChatEntry> {
        val kept = historyStore.load(vaultPath = vaultPath)

        val entries = kept.turns.map {
            ChatEntry.of(
                turn = it,
                id = nextId()
            )
        }

        summary = kept.summary
        compactedThrough = entries.getOrNull(index = kept.compacted - 1)?.id ?: NONE
        if (entries.isNotEmpty()) lastAsked = Instant.DISTANT_PAST

        return entries
    }

    /** What the question asked as entry [id] is given of the chat before it. */
    fun historyBefore(id: Long): List<ChatExchange> {
        val before = host
            .state
            .value
            .entries
            .filter {
                it.id < id
            }

        return memory.historyOf(conversation = conversationOf(entries = before))
    }

    /** The memory's use after the last answer; compacted when full or [isAsked]. */
    fun remember(isAsked: Boolean = false) {
        val usage = usage()
        host.reduce { it.copy(contextUsage = usage) }

        val isDue = (isAsked || usage >= 1f) && compaction?.isActive != true
        if (!isDue) return

        compaction = host.launch { compacted() }
    }

    /** The chat so far written into its summary before a question asked after a while. */
    suspend fun compactIfIdle() {
        val now = clock.now()

        val isIdle = lastAsked?.let {
            now - it >= IDLE
        } == true

        lastAsked = now
        if (isIdle && compaction?.isActive != true) compacted()
    }

    /** The conversation written to the vault's history; the memory measured again first. */
    fun save() {
        remember()
        keep()
    }

    /** The conversation written as it stands, a turn under way with it: closed now, the app shows it as stopped. */
    fun keep() {
        val conversation =
            conversationOf(entries = host.state.value.entries).last(count = MAX_TURNS)

        host.launch {
            historyStore.save(
                vaultPath = vaultPath,
                conversation = conversation
            )
        }
    }

    /** Nothing kept: the memory and the vault's history emptied. */
    fun clear() {
        // A compaction still running would bring the cleared chat back as its summary.
        compaction?.cancel()
        compaction = null
        summary = null
        compactedThrough = NONE
        host.launch { historyStore.clear(vaultPath = vaultPath) }
    }

    private fun usage(): Float =
        memory.usageOf(conversation = conversationOf(entries = host.state.value.entries))

    private fun conversationOf(entries: List<ChatEntry>): Conversation {
        val kept = entries.filter { it.isKept }

        return Conversation(
            turns = kept.map { it.toTurn() },
            summary = summary,
            compacted = kept.count {
                it.id <= compactedThrough
            }
        )
    }

    // The ring says Compacting only while this runs: ended, failed or cancelled, it says the memory again (the
    // Auto-Test on the real app, 2026-11-11, found it stuck on Compacting).
    private suspend fun compacted() {
        host.reduce { it.copy(isCompacting = true) }

        try {
            val kept = host
                .state
                .value
                .entries
                .filter { it.isKept }

            val after = memory.compact(conversation = conversationOf(entries = kept))
            summary = after.summary
            compactedThrough = kept.getOrNull(index = after.compacted - 1)?.id ?: NONE
        } finally {
            val usage = usage()

            host.reduce {
                it.copy(
                    isCompacting = false,
                    contextUsage = usage
                )
            }
        }
    }

    private companion object {
        const val MAX_TURNS = 100
        const val NONE = -1L
        val IDLE = 30.minutes
    }
}

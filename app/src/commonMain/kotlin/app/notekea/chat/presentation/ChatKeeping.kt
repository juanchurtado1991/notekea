package app.notekea.chat.presentation

import app.notekea.chat.domain.ChatHistoryStore
import kotlinx.coroutines.Job
import kotlin.time.Clock

/**
 * What the chat keeps: its turns in the vault's history ([historyStore], the last [MAX_TURNS]) and its memory, which
 * the last answer fills and which is compacted by itself once full, when asked, or before a question asked after a
 * while, as a new session starts.
 */
internal class ChatKeeping(
    private val vaultPath: String,
    private val historyStore: ChatHistoryStore,
    private val memory: ChatMemory,
    private val clock: Clock,
    private val host: ChatHost
) {
    private var compaction: Job? = null

    /** The memory's use after the last answer; compacted when full or [isAsked]. */
    fun remember(isAsked: Boolean = false) {
        val entries = host.state.value.entries
        val usage = memory.usageOf(entries = entries)
        host.reduce { it.copy(contextUsage = usage) }

        val isDue = (isAsked || usage >= 1f) && compaction?.isActive != true
        if (!isDue) return

        compaction = host.launch { compacted() }
    }

    /** The chat so far written into its summary before a question asked after a while. */
    suspend fun compactIfIdle() {
        val isIdle = memory.isIdleAt(now = clock.now())
        if (isIdle && compaction?.isActive != true) compacted()
    }

    /** The turns kept, written to the vault's history; the memory measured again first. */
    fun save() {
        remember()

        val turns = host
            .state
            .value
            .entries
            .filter { it.isKept }
            .takeLast(n = MAX_TURNS)
            .map { it.toTurn() }

        host.launch {
            historyStore.save(
                vaultPath = vaultPath,
                turns = turns
            )
        }
    }

    /** Nothing kept: the memory and the vault's history emptied. */
    fun clear() {
        memory.clear()
        host.launch { historyStore.clear(vaultPath = vaultPath) }
    }

    // The ring says Compacting only while this runs: ended, failed or cancelled, it says the memory again (the
    // Auto-Test on the real app, 2026-11-11, found it stuck on Compacting).
    private suspend fun compacted() {
        host.reduce { it.copy(isCompacting = true) }

        try {
            memory.compact(entries = host.state.value.entries)
        } finally {
            host.reduce {
                it.copy(
                    isCompacting = false,
                    contextUsage = memory.usageOf(entries = it.entries)
                )
            }
        }
    }

    private companion object {
        const val MAX_TURNS = 100
    }
}

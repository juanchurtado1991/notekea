package app.notekea.chat.eval

import app.notekea.chat.presentation.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

/** How full a probed chat's memory is, for the probe's report. */
internal object ProbeMemory {
    /** The chat's memory once a compaction under way ends, or after [COMPACT_SECONDS] if it never does. */
    suspend fun of(chat: ChatViewModel): String {
        val start = System.nanoTime()
        val limit = COMPACT_SECONDS * NANOS_PER_SECOND

        while (chat.state.value.isCompacting) {
            val isLate = System.nanoTime() - start >= limit
            if (isLate) break
            delay(timeMillis = SETTLE_MILLIS)
        }

        val state = chat.state.value
        val waited = (System.nanoTime() - start) / NANOS_PER_SECOND

        return "memory: ${(state.contextUsage * PERCENT).toInt()} %, compacting: ${state.isCompacting}, waited $waited s"
    }

    private const val SETTLE_MILLIS = 300L
    private const val NANOS_PER_SECOND = 1_000_000_000L
    private const val COMPACT_SECONDS = 120L
    private const val PERCENT = 100
}

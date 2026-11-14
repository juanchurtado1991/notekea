package app.notekea.ai.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

/**
 * The room the model's calls share: [total] tokens of one context among [slots] calls at most. A call waits while
 * its tokens and those of the calls running would pass the total; llama.cpp does not, and then fails them all,
 * the short with the long (measured on 2026-11-14, `docs/agent-loop.md`, step 0).
 */
class TokenRoom(
    private val total: Int,
    private val slots: Int
) {
    private val used = MutableStateFlow(value = Used())

    /** Whether a call of [tokens] can ever run: one longer than the whole context cannot. */
    fun fits(tokens: Int): Boolean = tokens <= total

    /** Runs [block] once [tokens] have room beside the calls running, and gives the room back when it ends. */
    suspend fun <T> withRoom(
        tokens: Int,
        block: suspend () -> T
    ): T {
        take(tokens = tokens)

        try {
            return block()
        } finally {
            used.update { it.without(tokens = tokens) }
        }
    }

    private suspend fun take(tokens: Int) {
        while (true) {
            val now = used.first {
                hasRoom(
                    used = it,
                    tokens = tokens
                )
            }

            if (used.compareAndSet(expect = now, update = now.with(tokens = tokens))) return
        }
    }

    private fun hasRoom(
        used: Used,
        tokens: Int
    ): Boolean = used.calls < slots && (used.calls == 0 || used.tokens + tokens <= total)

    private data class Used(
        val tokens: Int = 0,
        val calls: Int = 0
    ) {
        fun with(tokens: Int) = Used(
            tokens = this.tokens + tokens,
            calls = calls + 1
        )

        fun without(tokens: Int) = Used(
            tokens = this.tokens - tokens,
            calls = calls - 1
        )
    }
}

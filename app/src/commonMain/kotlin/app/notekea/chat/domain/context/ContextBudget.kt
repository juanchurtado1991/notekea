package app.notekea.chat.domain.context

import app.notekea.core.llm.domain.LlmEngine

/**
 * What a turn's prompt may hold of what the turn found, counted in the model's tokens (`docs/agent-loop.md`, step
 * 2): whole entries, the oldest dropped first and several at once, so the prompt's start stays as the model already
 * read it; the newest is always kept. One for a turn, which carries on what was dropped ([FittedContext.dropped]).
 */
class ContextBudget(
    private val llmEngine: LlmEngine,
    private val tokens: Int,
    private val fixed: String,
    private val replyTokens: Int
) {
    private val counted = mutableMapOf<String, Int>()

    /**
     * The [entries] that fit beside the fixed part (the instructions and the task) and the reply, oldest first, with
     * a line in place of the ones dropped. [droppedBefore] is what the turn's last fit dropped: it stays dropped, so
     * the prompt's start does not change at every move.
     */
    suspend fun fit(
        entries: List<String>,
        droppedBefore: Int = 0
    ): FittedContext {
        val room = tokens - countOf(text = fixed) - replyTokens - MARGIN_TOKENS
        val sizes = entries.map { countOf(text = it) }
        val most = (entries.size - 1).coerceAtLeast(minimumValue = 0)
        val before = droppedBefore.coerceAtMost(maximumValue = most)
        val isOver = sizes.drop(n = before).sum() > room

        // Down to a share of the room, not to its edge: the next entries then fit without dropping again.
        val dropped = if (!isOver) {
            before
        } else {
            (before..most).firstOrNull {
                sizes.drop(n = it).sum() <= room * KEPT_SHARE
            } ?: most
        }

        val kept = entries.drop(n = dropped)
        val shown = if (dropped == 0) kept else listOf(droppedLine(count = dropped)) + kept

        return FittedContext(
            entries = shown,
            dropped = dropped
        )
    }

    private suspend fun countOf(text: String): Int = counted
        .getOrPut(key = text) { llmEngine.countTokens(text = text) }

    companion object {
        private const val MARGIN_TOKENS = 64
        private const val KEPT_SHARE = 0.75

        /** Said in place of what was dropped, so the model knows something was there and does not look again blind. */
        fun droppedLine(count: Int): String =
            "(The $count earliest results are no longer shown, to keep within what can be read. " +
                "Their finds are in the answer so far; look one up again only if it is needed.)"
    }
}

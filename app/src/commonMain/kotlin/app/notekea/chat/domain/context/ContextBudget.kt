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
    private val replyTokens: Int,
    private val memory: StepMemory? = null,
    private val need: String = "",
    private val space: String = ""
) {
    private val counted = mutableMapOf<String, Int>()
    private val stored = mutableSetOf<Int>()

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

        if (dropped == 0) {
            return FittedContext(
                entries = kept,
                dropped = 0
            )
        }

        val line = droppedLine(
            count = dropped,
            canRecall = memory != null
        )

        val recalled = recalledOf(
            entries = entries,
            dropped = dropped,
            free = room - sizes.drop(n = dropped).sum() - countOf(text = line)
        )

        return FittedContext(
            entries = listOfNotNull(line, recalled) + kept,
            dropped = dropped
        )
    }

    /** Forgets what the turn kept aside: called once, when the turn ends. */
    suspend fun forget() {
        memory?.forget(space = space)
    }

    /**
     * What the model asks back with recall: the parts of the dropped results about [about], [RECALL_TOKENS] at most;
     * null when nothing is dropped or nothing matches.
     */
    suspend fun recalled(
        about: String,
        entries: List<String>,
        dropped: Int
    ): String? {
        if (dropped == 0) return null

        val parts = partsOf(
            entries = entries,
            dropped = dropped,
            need = about,
            room = RECALL_TOKENS
        ) ?: return "recall \"$about\": nothing about it in the earlier results. Look it up again if it is needed."

        return "recall \"$about\", from the earlier results:\n$parts"
    }

    // The parts of the dropped results the task needs, as many as the [free] room holds, in their steps' order: a
    // page read early is not lost to the notes read after it (`docs/agent-step-memory.md`).
    private suspend fun recalledOf(
        entries: List<String>,
        dropped: Int,
        free: Int
    ): String? = partsOf(
        entries = entries,
        dropped = dropped,
        need = need,
        room = free - countOf(text = RECALLED)
    )?.let {
        RECALLED + it
    }

    private suspend fun partsOf(
        entries: List<String>,
        dropped: Int,
        need: String,
        room: Int
    ): String? {
        val memory = memory ?: return null

        entries
            .indices
            .filterNot {
                it in stored
            }
            .forEach {
                memory.keep(
                    space = space,
                    entry = it,
                    chunks = StepChunks.of(entry = entries[it])
                )

                stored += it
            }

        val ranked = memory.ranked(
            space = space,
            need = need,
            entries = (0 until dropped).toSet(),
            most = MOST_RECALLED
        )

        val chosen = ranked
            .fold(initial = emptyList<Pair<StepChunk, String>>()) { taken, chunk ->
                val text = StepChunks
                    .of(entry = entries[chunk.entry])
                    .getOrNull(index = chunk.chunk)
                    ?: return@fold taken

                val used = taken.sumOf { countOf(text = it.second) }
                val isRoomy = used + countOf(text = text) <= room

                if (isRoomy) taken + (chunk to text) else taken
            }

        if (chosen.isEmpty()) return null

        return chosen
            .sortedWith(compareBy({ it.first.entry }, { it.first.chunk }))
            .groupBy { it.first.entry }
            .map { (entry, chunks) ->
                val heading = StepChunks.headingOf(entry = entries[entry])

                "From step ${entry + 1}, $heading:\n" + chunks
                    .joinToString(separator = "\n(…)\n") { it.second }
            }
            .joinToString(separator = "\n\n")
    }

    private suspend fun countOf(text: String): Int = counted
        .getOrPut(key = text) { llmEngine.countTokens(text = text) }

    companion object {
        private const val MARGIN_TOKENS = 64
        private const val MOST_RECALLED = 24
        private const val RECALL_TOKENS = 1_200

        const val RECALLED =
            "(Kept aside from those earlier results, the parts this task needs, in their steps' order:)\n"
        private const val KEPT_SHARE = 0.75

        /** Said in place of what was dropped, so the model knows something was there and does not look again blind. */
        fun droppedLine(
            count: Int,
            canRecall: Boolean = false
        ): String =
            "(The $count earliest results are no longer shown, to keep within what can be read. " +
                if (canRecall) {
                    "They are kept: recall what you need from them, rather than reading or searching again.)"
                } else {
                    "Their finds are in the answer so far; look one up again only if it is needed.)"
                }
    }
}

package app.notekea.chat.domain.context

import app.notekea.core.llm.domain.LlmEngine

/**
 * What a turn's prompt may hold of what the turn found, in the model's tokens (`docs/agent-loop.md`, step 2): whole
 * entries, the oldest dropped first and several at once; the newest always kept. With a [memory] and [wholeLast], the
 * step's prompt shows less on purpose ([shown]); one budget a turn, carrying on [FittedContext.dropped].
 */
class ContextBudget(
    private val llmEngine: LlmEngine,
    private val tokens: Int,
    private val fixed: String,
    private val replyTokens: Int,
    private val memory: StepMemory? = null,
    private val need: String = "",
    private val space: String = "",
    private val wholeLast: Int? = null
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

    /**
     * What the model is shown to pick its next step: [fit], with every long result before the [wholeLast] newest
     * folded to its first line and kept aside, so a small model reads less and asks back what it needs. The answer
     * is written from [fit], which shows them whole.
     */
    suspend fun shown(
        entries: List<String>,
        droppedBefore: Int = 0
    ): FittedContext {
        val fitted = fit(entries, droppedBefore)
        val whole = wholeLast ?: return fitted
        if (memory == null) return fitted

        val firstWhole = (entries.size - whole).coerceAtLeast(minimumValue = fitted.dropped)

        val folded = (fitted.dropped until firstWhole)
            .filter {
                countOf(text = entries[it]) > FOLDED_TOKENS
            }
            .toSet()

        if (folded.isEmpty()) return fitted

        val lead = fitted.entries.size - (entries.size - fitted.dropped)

        val shownEntries = (fitted.dropped until entries.size).map {
            if (it in folded) {
                foldedLine(
                    step = it + 1,
                    entry = entries[it]
                )
            } else {
                entries[it]
            }
        }

        return fitted.copy(
            entries = fitted.entries.take(n = lead) + shownEntries,
            folded = folded
        )
    }

    /** Forgets what the turn kept aside: called once, when the turn ends. */
    suspend fun forget() {
        memory?.forget(space = space)
    }

    /**
     * What the model asks back with recall: the parts of the [hidden] results (dropped or folded) about [about],
     * [RECALL_TOKENS] at most; null when nothing is hidden.
     */
    suspend fun recalled(
        about: String,
        entries: List<String>,
        hidden: Set<Int>
    ): String? {
        if (hidden.isEmpty()) return null

        val parts = partsOf(
            entries = entries,
            hidden = hidden,
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
        hidden = (0 until dropped).toSet(),
        need = need,
        room = free - countOf(text = RECALLED)
    )?.let {
        RECALLED + it
    }

    private suspend fun partsOf(
        entries: List<String>,
        hidden: Set<Int>,
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
            entries = hidden,
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

        // A result this short is shown whole even when old: its first line would save nothing.
        private const val FOLDED_TOKENS = 120

        /** Said in place of a result kept aside, so the model knows what it was and asks it back by name. */
        fun foldedLine(
            step: Int,
            entry: String
        ): String =
            "(Step $step, kept aside: recall it to see it whole. It began: ${StepChunks.headingOf(entry)})"

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

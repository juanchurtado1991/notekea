package app.notekea.chat.domain.context

import app.notekea.core.llm.domain.LlmEngine
import kotlin.math.pow

/**
 * What a turn's prompt may hold of what it found, in the model's tokens (`docs/agent-loop.md`, step 2): the oldest
 * dropped first, the newest kept and cut to the room when it alone is over it. With a [memory] and [wholeLast], the
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

        val line = droppedLine(
            count = dropped,
            canRecall = memory != null
        ).takeIf {
            dropped > 0
        }

        val lineTokens = if (line == null) 0 else countOf(text = line)

        val kept = keptOf(
            entries = entries.drop(n = dropped),
            room = room - lineTokens
        )

        if (line == null) {
            return FittedContext(
                entries = kept,
                dropped = 0
            )
        }

        val recalled = recalledOf(
            entries = entries,
            dropped = dropped,
            free = room - kept.sumOf { countOf(text = it) } - countOf(text = line)
        )

        return FittedContext(
            entries = listOfNotNull(line, recalled) + kept,
            dropped = dropped
        )
    }

    // The newest entry alone over the room overflowed the call, which was then taken for an unreadable one (the bug
    // hunt of 2026-11-17, E4): it is cut to the room, its start kept, rather than sent whole.
    private suspend fun keptOf(
        entries: List<String>,
        room: Int
    ): List<String> {
        val newest = entries.singleOrNull() ?: return entries
        val size = countOf(text = newest)
        if (size <= room) return entries

        val share = room.coerceAtLeast(minimumValue = 0).toDouble() / size

        val cut = (0..MOST_CUTS)
            .map {
                val length = newest.length * share * CUT_STEP.pow(n = it)

                newest.take(n = length.toInt()) + CUT
            }
            .firstOrNull {
                countOf(text = it) <= room
            } ?: CUT

        return listOf(cut)
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

        // The fitted entries, not the found ones: the newest may have been cut to the room.
        val shownEntries = fitted
            .entries
            .drop(n = lead)
            .mapIndexed { index, entry ->
                val at = fitted.dropped + index

                if (at in folded) {
                    foldedLine(
                        step = at + 1,
                        entry = entries[at]
                    )
                } else {
                    entry
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

        /** Said where the newest entry was cut to fit what can be read. */
        const val CUT = "\n(Cut here, to keep within what can be read.)"

        // Each try cuts a tenth more, as tokens are not spread evenly over a text.
        private const val CUT_STEP = 0.9
        private const val MOST_CUTS = 20

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

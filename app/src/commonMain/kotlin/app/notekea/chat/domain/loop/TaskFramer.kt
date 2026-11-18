package app.notekea.chat.domain.loop

import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.memory.domain.MemoryLine

/**
 * The first step of a turn on notes (`docs/agent-frame.md`). The code finds the sets of notes the task's words fit
 * alike and settles those their names tell apart ([FrameGroups]); the model answers small choices: how many things,
 * whether the task says which of a set, which note, which entry of it. Null when nothing is shown or read.
 */
class TaskFramer(llmEngine: LlmEngine) {
    private val asking = FrameQuestions(llmEngine = llmEngine)

    internal suspend fun frameOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>,
        isChange: Boolean = false
    ): TaskFrame? {
        if (candidates.isEmpty()) return null

        val (count, words) = asking.countOf(
            task = task,
            memory = memory
        ) ?: return null

        val targets = when (count) {
            FrameCount.ONE -> oneOf(task, candidates, memory, isChange)

            FrameCount.SEVERAL -> asking.severalOf(task, candidates, memory)?.map {
                FrameTarget(note = it.path)
            }

            else -> emptyList()
        } ?: return null

        return TaskFrame(
            targets = targets,
            count = count,
            candidates = candidates.map { it.path },
            words = words
        )
    }

    // A set of namesakes the task does not say which of is open; else the note meant, and for a change the entries
    // of it the task could mean.
    private suspend fun oneOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>,
        isChange: Boolean
    ): List<FrameTarget>? {
        val group = FrameGroups
            .of(
                task = task,
                candidates = candidates
            )
            .firstOrNull()

        // The names settle it when only one of the set has a word the task writes; else the model reads the notes.
        val said = group?.let {
            FrameGroups.settledOf(task, it) ?: asking.saidOf(task, it, memory)
        }

        if (group != null && said == null) return group.map { FrameTarget(note = it.path) }

        val meant = said ?: asking.pickOf(task, candidates, memory) ?: return emptyList()

        val entries = if (isChange && meant.entries.size > 1) {
            asking.entriesOf(task, meant, memory)
        } else {
            emptyList()
        }

        return listOf(
            FrameTarget(
                note = meant.path,
                entries = entries
            )
        )
    }
}

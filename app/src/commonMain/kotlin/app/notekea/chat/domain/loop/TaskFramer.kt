package app.notekea.chat.domain.loop

import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.memory.domain.MemoryLine

/**
 * The first step of a turn on notes (`docs/agent-frame.md`), as two small questions a small model answers well: how
 * many things the task means, from its words alone; then which shown note (and entry) it means, or that its words
 * fit several equally. Null when nothing is shown or a reply cannot be read: no frame, nothing held back for one.
 */
class TaskFramer(llmEngine: LlmEngine) {
    private val asking = FrameQuestions(llmEngine = llmEngine)

    internal suspend fun frameOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): TaskFrame? {
        if (candidates.isEmpty()) return null

        val (count, words) = asking.countOf(
            task = task,
            memory = memory
        ) ?: return null

        val targets = when (count) {
            FrameCount.ONE -> asking.oneOf(task, candidates, memory)
            FrameCount.SEVERAL -> asking.severalOf(task, candidates, memory)
            else -> emptyList()
        } ?: return null

        return TaskFrame(
            targets = targets,
            count = count,
            candidates = candidates.map { it.path },
            words = words
        )
    }
}

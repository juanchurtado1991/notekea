package app.notekea.chat.domain.loop

import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.memory.domain.MemoryLine

/**
 * The first step of every turn on notes (`docs/agent-frame.md`). The code finds the sets of notes the task's words
 * fit alike and settles those their names or the memory tell apart; the model marks each of the rest yes or no, and
 * the code counts. Then, for an item inside a note, the same for its entries. Null when nothing is shown or read.
 */
class TaskFramer(llmEngine: LlmEngine) {
    private val asking = FrameQuestions(llmEngine = llmEngine)

    internal suspend fun frameOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): TaskFrame? {
        if (candidates.isEmpty()) return null

        val reply = asking.countOf(
            task = task,
            memory = memory
        ) ?: return null

        val (targets, reason) = when (reply.count) {
            FrameCount.ONE -> oneOf(task, reply, candidates, memory)

            FrameCount.SEVERAL -> asking.severalOf(task, candidates, memory)?.let { several ->
                targetsOf(several) to FrameReason.NONE
            }

            else -> emptyList<FrameTarget>() to FrameReason.NONE
        } ?: return null

        return TaskFrame(
            targets = targets,
            count = reply.count,
            candidates = candidates.map { it.path },
            words = reply.words,
            reason = reason
        )
    }

    // The one note the task means, or the notes it could mean when several fit; then, for an item inside the note,
    // the entries it could be about. With what settled it, or left it open.
    private suspend fun oneOf(
        task: String,
        reply: FrameCountReply,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): Pair<List<FrameTarget>, FrameReason>? {
        // The set is made by the task's own words for its thing, not every word it writes: in "a property budget on
        // the garden plan", budget names the property, not the note (the loop's run of 2026-11-17).
        val group = FrameGroups
            .of(
                task = reply.words.ifBlank { task },
                candidates = candidates
            )
            .firstOrNull()

        val (fits, reason) = group
            ?.let {
                fitsOf(task, it, memory) ?: return null
            }
            ?: (emptyList<FrameCandidates.Candidate>() to FrameReason.PICK)

        if (fits.size > 1) return targetsOf(fits) to reason

        val picked = fits.singleOrNull()
            ?: asking.pickOf(task, candidates, memory)
            ?: return emptyList<FrameTarget>() to FrameReason.PICK

        // The note picked has namesakes the task's words did not find, in another language ("el presupuesto" for two
        // Budget 2026): the same question, which of them the task could mean.
        val namesakes = candidates.filter {
            it
                .path
                .nameWithoutExtension
                .equals(
                    other = picked.path.nameWithoutExtension,
                    ignoreCase = true
                )
        }

        val pickedBy = if (fits.isEmpty()) FrameReason.PICK else reason

        // Marked none of the namesakes: as if there were no set, the note picked.
        val (meant, by) = if (group == null && namesakes.size > 1) {
            val (fitting, said) = fitsOf(task, namesakes, memory) ?: return null
            if (fitting.size > 1) return targetsOf(fitting) to said

            fitting.singleOrNull()?.let {
                it to said
            } ?: (picked to pickedBy)
        } else {
            picked to pickedBy
        }

        return entriesOf(task, reply, meant, memory, by)
    }

    // The entries of the note meant the task could be about, when its thing is an item and the note has several:
    // one fits, that entry; several, open between them; none, the whole note.
    private suspend fun entriesOf(
        task: String,
        reply: FrameCountReply,
        meant: FrameCandidates.Candidate,
        memory: List<MemoryLine>,
        reason: FrameReason
    ): Pair<List<FrameTarget>, FrameReason>? {
        val entries = if (reply.isItem && meant.entries.size > 1) {
            asking.markedEntriesOf(task, meant, memory) ?: return null
        } else {
            emptyList()
        }

        val target = FrameTarget(
            note = meant.path,
            entries = entries
        )

        return listOf(target) to if (entries.isEmpty()) reason else FrameReason.ENTRY
    }

    // Which of a set the task could mean: its own words first ("Laura Castro's email"), then the memory ("Laura" is
    // [[Laura Castro]]), both by the code; only then the model's marks. Null when the marks cannot be read.
    private suspend fun fitsOf(
        task: String,
        group: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): Pair<List<FrameCandidates.Candidate>, FrameReason>? {
        FrameGroups.settledOf(task, group)?.let {
            return listOf(it) to FrameReason.NAMES
        }

        FrameMemory.settledOf(task, group, memory)?.let {
            return listOf(it) to FrameReason.MEMORY
        }

        return asking.markedNotesOf(task, group, memory)?.let {
            it to FrameReason.MARKS
        }
    }

    private fun targetsOf(group: List<FrameCandidates.Candidate>): List<FrameTarget> =
        group.map { FrameTarget(note = it.path) }
}

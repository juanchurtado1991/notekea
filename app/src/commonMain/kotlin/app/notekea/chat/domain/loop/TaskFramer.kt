package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ModelJson
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.memory.domain.MemoryLine

/**
 * The first step of every turn on notes (`docs/agent-frame.md`). The code finds the sets of notes the task's words
 * fit alike and settles those their names or the memory tell apart; the model marks each of the rest yes or no, and
 * the code counts. Then the same for the entries of an item. Null when nothing is shown or a reply is unread.
 */
class TaskFramer(
    llmEngine: LlmEngine,
    modelJson: ModelJson
) {
    private val asking = FrameQuestions(
        llmEngine = llmEngine,
        modelJson = modelJson
    )

    internal suspend fun frameOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>,
        isChange: Boolean = false
    ): TaskFrame? {
        if (candidates.isEmpty()) return null

        val reply = asking.countOf(
            task = task,
            memory = memory
        ) ?: return null

        val (targets, reason) = when (reply.count) {
            FrameCount.ONE -> {
                val asked = reply.copy(isItem = reply.isItem && isChange)
                oneOf(task, asked, candidates, memory)
            }

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
        val words = reply.words.ifBlank { task }

        // What tells it from another of its kind is no word of its name: "Rita, the one from the book club".
        val told = "$words ${reply.which}"

        val group = FrameGroups
            .of(
                task = words,
                candidates = candidates
            )
            .firstOrNull()

        val (fits, reason) = group
            ?.let {
                fitsOf(task, told, it, memory) ?: return null
            }
            ?: (emptyList<FrameCandidates.Candidate>() to FrameReason.PICK)

        // None marked is no answer either: the 4B marks every one "no" when it cannot tell them apart (the frame
        // alone, 2026-11-18), so the set stays open, as when several are marked.
        val open = group
            ?.takeIf {
                fits.size != 1
            }
            ?.let {
                fits.ifEmpty { it }
            }
            .orEmpty()

        val rivals = FrameGroups.rivalsOf(group = open)
        if (rivals.isNotEmpty()) return targetsOf(rivals) to reason

        // An item no note is named for, held by several notes of one kind, is open between them: none is picked.
        val holders = if (group == null && reply.isItem) {
            FrameGroups.rivalsOf(group = ItemHolders.of(reply.words, candidates))
        } else {
            emptyList()
        }

        if (holders.size > 1) return targetsOf(holders) to FrameReason.WORDS

        // Notes of other kinds that share a word are told apart by what the task is about: the model picks one.
        val picked = fits.singleOrNull()
            ?: asking.pickOf(task, open.ifEmpty { candidates }, memory)
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

        val (meant, by) = if (group == null && namesakes.size > 1) {
            val (fitting, said) = fitsOf(task, told, namesakes, memory) ?: return null
            if (fitting.size != 1) return targetsOf(fitting.ifEmpty { namesakes }) to said

            fitting.single() to said
        } else {
            picked to if (fits.size == 1) reason else FrameReason.PICK
        }

        return entriesOf(task, reply, meant, memory, by)
    }

    // The entries of the note meant a change could be about, when its thing is an item and the note has several
    // (a question about "the budget" sums its rows: marking them asked on 26 of 319 tasks, the frame alone):
    // one fits, that entry; several, open between them; none, the whole note.
    private suspend fun entriesOf(
        task: String,
        reply: FrameCountReply,
        meant: FrameCandidates.Candidate,
        memory: List<MemoryLine>,
        reason: FrameReason
    ): Pair<List<FrameTarget>, FrameReason>? {
        // None marked is the whole note: leaving them open made the 4B ask on 99 of 319 tasks that need no
        // question (the frame alone, 2026-11-18), as it marks every entry "no" for a question about the note.
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
        words: String,
        group: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): Pair<List<FrameCandidates.Candidate>, FrameReason>? {
        FrameGroups.settledOf(task, group)?.let {
            return listOf(it) to FrameReason.NAMES
        }

        FrameMemory.settledOf(task, group, memory)?.let {
            return listOf(it) to FrameReason.MEMORY
        }

        // Then the words only one note writes, by the code; none is nothing to tell them apart by. Only when
        // several notes write words of their own is the model asked: the 4B marked one note of two Lauras "yes"
        // with nothing to tell them apart (the frame alone, 2026-11-18). Only the task's words for its thing tell
        // them apart: "call him about the March trip" told one of two Toms by a note that writes March.
        val told = FrameGroups.toldOf(words, group)
        if (told.size < 2) return told.ifEmpty { group } to FrameReason.WORDS

        return asking.markedNotesOf(task, told, memory)?.let {
            it to FrameReason.MARKS
        }
    }

    private fun targetsOf(group: List<FrameCandidates.Candidate>): List<FrameTarget> =
        group.map { FrameTarget(note = it.path) }
}

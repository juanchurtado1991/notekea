package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.Remembering
import app.notekea.core.notes.domain.RelativePath

/**
 * What a task is about (`docs/agent-frame.md`): every note, and entry inside one, of those shown ([candidates]) the
 * task's words fit, how many it means ([count]) and what settled it ([reason]). One meant and several fitting is open.
 */
data class TaskFrame(
    val targets: List<FrameTarget>,
    val count: FrameCount,
    val candidates: List<RelativePath>,
    val words: String? = null,
    val reason: FrameReason = FrameReason.NONE
) {
    val notes: List<RelativePath> = targets.map { it.note }.distinct()

    private val entries: List<String> = targets.flatMap { it.entries }.distinct()

    private val isOne = count == FrameCount.ONE

    /** The notes the person must choose between: the task means one, and its words fit several. */
    val openNotes: List<RelativePath> = notes.takeIf {
        isOne && it.size > 1
    }.orEmpty()

    /** The entries of the one note the person must choose between: "the appointment" in a note of three. */
    val openEntries: List<String> = entries.takeIf {
        isOne &&
            notes.size == 1 &&
            it.size > 1
    }.orEmpty()

    val isOpen: Boolean = openNotes.isNotEmpty() || openEntries.isNotEmpty()

    /** The one note the task means, when its words fit that one alone. */
    val meant: RelativePath? = notes.singleOrNull()?.takeIf { isOne }

    /** The one entry of [meant] the task means, when it is about one entry and fits that one alone. */
    val entry: String? = entries.singleOrNull()?.takeIf {
        meant != null
    }

    /** Whether the task means more than one thing: those it names, or every one a condition picks out. */
    val isSeveral: Boolean = count == FrameCount.SEVERAL || count == FrameCount.EVERY

    /** What the person chooses between when [isOpen]: the notes by their paths, or the entries as written. */
    val options: List<String> = openNotes.map { it.bare() }.ifEmpty { openEntries }

    /** The other notes shown for the task, which it does not mean: a change or an answer from one is held back. */
    val others: List<RelativePath> = candidates.filter {
        it !in notes
    }

    /**
     * What the question about [openNotes] can be remembered as, once the person picks one (`docs/agent-memory.md`):
     * the task's own [words] that meant them, when the task writes them. Never for entries, which are no names.
     */
    fun rememberingOf(task: String): Remembering? {
        val said = words
            ?.trim()
            ?.takeIf {
                it.isNotEmpty() && task.contains(other = it, ignoreCase = true)
            }

        return said
            ?.takeIf { openNotes.isNotEmpty() }
            ?.let {
                Remembering(
                    said = it,
                    notes = openNotes
                )
            }
    }

    /** The frame as the loop's prompt tells it, so the model works on what was settled. */
    fun described(): String = when {
        openNotes.isNotEmpty() ->
            "The task means one note and its words fit ${openNotes.size}: ${options.joinToString()}. Nothing " +
                "says which: ask the person with ask_user, these as the options, before changing or answering."

        openEntries.isNotEmpty() ->
            "The task means ${notes.single().bare()}. When it changes one entry already written there, its " +
                "words fit ${openEntries.size}: ${openEntries.joinToString(separator = "; ")}. Ask the person " +
                "which with ask_user, these as the options, before changing it. A change to the note as a " +
                "whole (a tag, a property, a link, a rename) needs no question: make it."

        meant != null ->
            "The task means ${meant.bare()}" + entry?.let { ", its entry \"$it\"" }.orEmpty() +
                ". Work on it; never ask which note."

        count == FrameCount.SEVERAL ->
            "The task means each of: ${notes.joinToString { it.bare() }}. Work on all of them; never ask which."

        count == FrameCount.EVERY ->
            "The task means every note its condition picks out" +
                notes
                    .takeIf { it.isNotEmpty() }
                    ?.let { n -> ", among them ${n.joinToString { it.bare() }}" }
                    .orEmpty() + ". Find them all; never ask which."

        else -> ""
    }
}

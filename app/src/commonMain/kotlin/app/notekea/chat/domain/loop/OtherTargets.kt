package app.notekea.chat.domain.loop

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.ModelJson
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * The last look before a change to one thing is proposed: whether the request could be about another one as well. The
 * frame judges before anything is read; here the note and the line to change are known, so the code finds those beside
 * it (the other entries of its section, the notes its name is shared with) and the model marks each, as in the frame.
 */
internal class OtherTargets(
    llmEngine: LlmEngine,
    modelJson: ModelJson,
    private val vaultGuide: VaultGuide
) {
    private val asking = FrameQuestions(
        llmEngine = llmEngine,
        modelJson = modelJson
    )

    /** What the model is told when [call] proposes a change the request could mean of several, else null. */
    suspend fun heldOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        val frame = state.frame?.takeIf {
            it.count == FrameCount.ONE && !it.isOpen
        } ?: return null

        val isLooked = call.name != ProposeTool.NAME || OPEN in state.told
        if (isLooked) return null

        val target = targetOf(state = state) ?: return null

        // The open note ("this note") and one the memory names are the person's own words for it.
        val isSaid = target == state.turn.openNote || frame.reason == FrameReason.MEMORY
        if (isSaid) return null

        val alike = alikeOf(target, state)
        val notes = notesOf(alike, frame, state)

        val options = notes
            .map { it.bare() }
            .ifEmpty { entriesOf(target, alike, frame, state) }

        if (options.size < 2) return null

        state.told += OPEN
        state.told += UnsettledEnd.MUST_ASK

        return "(Not proposed: the request could be about any of ${options.size}, and does not say which: " +
            "${options.joinToString(separator = "; ")}. Ask the person with ask_user, these as the options.)"
    }

    // The one note the changes act on; null when they act on several, or write a new one.
    private fun targetOf(state: TurnState): RelativePath? {
        val changes = state.turn.work.changes
        if (changes.any { it.kind !in CHANGING }) return null

        return changes
            .flatMap {
                it.notes + listOfNotNull(it[ActionField.NOTE])
            }
            .map { state.turn.noteNamed(name = it) }
            .distinct()
            .singleOrNull()
    }

    // The notes whose names write the request's words as the target's does, the target among them.
    private fun alikeOf(
        target: RelativePath,
        state: TurnState
    ): List<NoteEntry> {
        val words = FrameWords.of(text = state.task)

        val shared = wordsOf(target).filter { word ->
            words.any {
                FrameWords.isSame(
                    one = it,
                    other = word
                )
            }
        }

        if (shared.isEmpty()) return emptyList()

        return state
            .turn
            .notes
            .filter { it.file.path.isMarkdown }
            .filter { entry ->
                val own = wordsOf(entry.file.path)

                shared.all { word ->
                    own.any {
                        FrameWords.isSame(
                            one = it,
                            other = word
                        )
                    }
                }
            }
    }

    // Those of [alike] the request could be about, marked by the model; none when its words name the target alone.
    private suspend fun notesOf(
        alike: List<NoteEntry>,
        frame: TaskFrame,
        state: TurnState
    ): List<RelativePath> {
        if (alike.size !in 2..MOST_NOTES) return emptyList()

        val texts = vaultGuide.textsOf(
            vaultPath = state.turn.vaultPath,
            notes = alike
        )

        val group = FrameCandidates
            .of(
                task = state.task,
                paths = alike.map { it.file.path },
                texts = texts
            )
            .map { it.copy(lines = it.entries.take(n = SHOWN)) }

        // The words for the thing name the target alone: "my lab budget" for Lab/Budget.
        val isNamed = FrameGroups.settledOf(
            task = frame.words.orEmpty(),
            group = group
        ) != null

        if (isNamed) return emptyList()

        return asking
            .markedNotesOf(state.task, group, state.memory)
            .orEmpty()
            .map { it.path }
    }

    // The entries the request could be about, marked by the model: those of the changed line's section, or, when
    // something is added to the note, those that write the request's words for its thing.
    private suspend fun entriesOf(
        target: RelativePath,
        alike: List<NoteEntry>,
        frame: TaskFrame,
        state: TurnState
    ): List<String> {
        val changes = state.turn.work.changes

        val paths = listOf(target) + alike.map { it.file.path }
        val texts = textsOf(paths, frame, state)
        val line = lineOf(changes = changes)

        val isAdded = changes.all {
            it.kind in ADDING
        }

        val cover = line?.let { RequestCover.of(state.task, it, target, texts, paths) }

        val entries = when {
            cover != null -> cover.entries.map { it.optionFor(target = target) }

            isAdded -> FrameCandidates.touchedOf(
                words = FrameWords.of(text = frame.words.orEmpty()) - wordsOf(target),
                text = texts[target].orEmpty()
            )

            else -> emptyList()
        }

        val isShort = entries.size <= MOST_SHOWN

        if (entries.size < 2) return emptyList()

        // Several cover more of the request than the line chosen: it is not the one, and which is, the person says.
        val isAnother = isShort && cover?.isBetter == true
        if (isAnother) return entries

        val note = FrameCandidates.Candidate(
            path = target,
            gist = "",
            entries = entries
        )

        val marked = asking
            .markedEntriesOf(state.task, note, state.memory)
            .orEmpty()

        if (marked.size < 2) return emptyList()

        // Two marked say it is open; the one meant may be one not marked: a short list goes whole.
        return if (isShort) entries else marked
    }

    // The note changed, those the turn read and those the frame was shown: where the thing meant may be written.
    private suspend fun textsOf(
        first: List<RelativePath>,
        frame: TaskFrame,
        state: TurnState
    ): Map<RelativePath, String> {
        val paths = (first + state.turn.reads.read + frame.candidates).distinct()

        return vaultGuide.textsOf(
            vaultPath = state.turn.vaultPath,
            notes = state
                .turn
                .notes
                .filter {
                    it.file.path in paths
                }
        )
    }

    // The one line changed or task completed; null when the changes touch several, or none.
    private fun lineOf(changes: List<PlannedStep>): String? = changes
        .mapNotNull {
            when (it.kind) {
                ActionKind.CHANGE_LINE -> it[ActionField.LINE]
                ActionKind.COMPLETE_TASK -> it[ActionField.TEXT]
                else -> null
            }
        }
        .distinct()
        .singleOrNull()
        ?.takeIf {
            changes.size == 1
        }

    private fun wordsOf(path: RelativePath): Set<String> =
        FrameWords.of(text = path.nameWithoutExtension)

    private companion object {
        const val OPEN = "other targets"
        const val MOST_NOTES = 6
        const val SHOWN = 8
        const val MOST_SHOWN = 4

        val ADDING = setOf(
            ActionKind.APPEND_TEXT,
            ActionKind.ADD_TASK
        )

        val CHANGING = setOf(
            ActionKind.APPEND_TEXT,
            ActionKind.SET_PROPERTY,
            ActionKind.ADD_TAG,
            ActionKind.ADD_TASK,
            ActionKind.COMPLETE_TASK,
            ActionKind.CHANGE_LINE,
            ActionKind.RENAME,
            ActionKind.MOVE,
            ActionKind.TRASH
        )
    }
}

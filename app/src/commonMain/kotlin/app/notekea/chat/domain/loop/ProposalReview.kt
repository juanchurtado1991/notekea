package app.notekea.chat.domain.loop

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.NoteProperties
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.core.notes.domain.RelativePath

/**
 * The notes of a change to many, listed back once before the card with what each holds, so the model drops those
 * the task does not ask for (`docs/agent-loop.md`, "Why the loop fails", item 4).
 */
internal object ProposalReview {
    /** What the model is told when [call] proposes changes to several notes for the first time, else null. */
    fun heldOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        val notes = notesOf(state = state)
        val isFirst = call.name == ProposeTool.NAME && notes.size >= MANY

        if (!isFirst || !state.told.add(REVIEWED)) return null

        val listed = notes
            .joinToString(separator = "\n") { name ->
                "- $name: ${heldBy(name = name, state = state)}"
            }

        return "(Not proposed yet. The changes act on these notes; check each against the task by what it " +
            "holds:\n$listed\nA note that lists or links the others (a reading list, a menu, a home page) is " +
            "not one of them. Call propose again, with drop naming each note the task does not ask for, or " +
            "none.)"
    }

    /** The notes [call] drops taken out of the turn's changes, by their names or paths. */
    fun dropped(
        call: ToolCall,
        state: TurnState
    ) {
        val dropped = call
            .list(name = ProposeTool.DROP)
            .mapNotNull { state.turn.noteNamed(name = it) }
            .toSet()

        if (dropped.isEmpty()) return

        state
            .turn
            .work
            .drop {
                state.turn.noteNamed(name = it) in dropped
            }

        // A change taken out may be asked for again: its call is no longer one already made (the loop's eighth run
        // answered "is in the proposal" for a change the drop had removed, and the turn ended with nothing).
        state.made.removeAll { key ->
            state.tools[key.substringBefore(delimiter = ':')]?.effect == ToolEffect.PROPOSES
        }
    }

    // The notes the changes act on, each once, as they were written: a new note's sources are what it is written
    // from, and dropping them emptied the note (the loop's fifth run, eight notes on the 4B never written).
    private fun notesOf(state: TurnState): List<String> = state
        .turn
        .work
        .changes
        .filter {
            it.kind != ActionKind.WRITE_FROM_NOTES
        }
        .flatMap {
            it.notes + listOfNotNull(it[ActionField.NOTE])
        }
        .distinctBy {
            state.turn.noteNamed(name = it) ?: it
        }

    // Its properties, tags and open tasks: what a condition of the task is checked on.
    private fun heldBy(
        name: String,
        state: TurnState
    ): String {
        val path: RelativePath? = state.turn.noteNamed(name = name)

        val note = state
            .turn
            .notes
            .firstOrNull {
                it.file.path == path
            } ?: return "not in the vault"

        val properties = NoteProperties.of(note = note)

        val open = note
            .metadata
            .tasks
            .count {
                !it.isDone
            }

        val tasks = if (open > 0) listOf("$open open tasks") else emptyList()

        return (properties + tasks)
            .joinToString(separator = "; ")
            .ifEmpty { "no properties" }
    }

    private const val MANY = 2
    private const val REVIEWED = "proposal reviewed"
}

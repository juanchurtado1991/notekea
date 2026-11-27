package app.notekea.chat.domain.tool.work

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlannedStep

/** A turn's work (`docs/agent-loop.md`): the changes it adds to its proposal, and the warnings it was given once. */
class TurnWork {
    private val proposed = mutableListOf<PlannedStep>()
    private val warned = mutableSetOf<String>()

    /** The changes the turn has added to its proposal, in order: what the card shows when it ends on `propose`. */
    val changes: List<PlannedStep> get() = proposed.toList()

    /** Whether the turn is warned [about] something for the first time: a warning is given once. */
    fun warn(about: String): Boolean = warned.add(about)

    fun change(step: PlannedStep) {
        proposed += step
    }

    /** [earlier] in the proposal put right by [step], in its place: a change corrected is not added beside it. */
    fun replace(
        earlier: PlannedStep,
        step: PlannedStep
    ) {
        val at = proposed.indexOf(element = earlier)

        if (at < 0) proposed += step else proposed[at] = step
    }

    /**
     * The notes [isDropped] picks taken out of the changes; a change left with none goes. A new note's sources are
     * not what it changes: it is kept as it is.
     */
    fun drop(isDropped: (String) -> Boolean) {
        val kept = proposed.mapNotNull { step ->
            if (step.kind == ActionKind.WRITE_FROM_NOTES) return@mapNotNull step

            val notes = step.notes.filterNot(isDropped)
            val isGone = step[ActionField.NOTE]?.let(isDropped) == true

            when {
                isGone -> null
                step.notes.isNotEmpty() && notes.isEmpty() -> null
                else -> step.copy(notes = notes)
            }
        }

        proposed.clear()
        proposed += kept
    }
}

package app.notekea.chat.domain.agent

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.Placeholders
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.prompt.AgentTaskPrompt
import app.notekea.core.notes.domain.RelativePath

/**
 * A proposal's notes written from what the agent read, not from what the model knows: a note it creates after
 * reading notes is written from those notes, read whole (the developer's smoke of 2026-11-14: a table of a
 * contract's pros and cons came as examples to replace).
 */
internal object GroundedSteps {
    /** The steps whose text the model writes. */
    val WRITTEN = setOf(ActionKind.CREATE_NOTE, ActionKind.WRITE_FROM_NOTES)

    private const val MARKDOWN = ".md"

    fun of(
        move: AgentTaskMove,
        read: List<RelativePath>
    ): AgentTaskMove {
        if (read.isEmpty()) return move

        val names = read.map { it.value.removeSuffix(suffix = MARKDOWN) }

        return move.copy(
            steps = move.steps.map { step ->
                val isFree =
                    step.kind == ActionKind.CREATE_NOTE && step[ActionField.TEMPLATE] == null

                val isUnnamed = step.kind == ActionKind.WRITE_FROM_NOTES && step.notes.isEmpty()

                when {
                    isFree -> step.copy(
                        kind = ActionKind.WRITE_FROM_NOTES,
                        fields = step.fields + (ActionField.TEXT to whatOf(step = step)),
                        notes = names
                    )

                    isUnnamed -> step.copy(notes = names)

                    else -> step
                }
            }
        )
    }

    /**
     * The proposal a task ends on when the model ends without one: the [planned] steps that write a note, written
     * from what was [read]; null when the plan wrote none or nothing was read.
     */
    fun asked(
        planned: List<PlannedStep>,
        read: List<RelativePath>
    ): AgentTaskMove? {
        val isWriting =
            planned.isNotEmpty() &&
                planned.all { it.kind.isConfirmed } &&
                planned.any { it.kind in WRITTEN }

        if (!isWriting || read.isEmpty()) return null

        return of(
            move = AgentTaskMove(
                move = AgentTaskPrompt.PROPOSE,
                steps = planned
            ),
            read = read
        )
    }

    // What to write, in the model's words; the note itself written there as placeholders says only its title.
    private fun whatOf(step: PlannedStep): String {
        val title = step[ActionField.TITLE].orEmpty()

        return step[ActionField.TEXT]
            ?.takeIf {
                !Placeholders.isIn(text = it) && it.length <= MAX_WHAT
            }
            ?: title
    }

    private const val MAX_WHAT = 300
}

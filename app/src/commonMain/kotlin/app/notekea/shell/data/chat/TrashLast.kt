package app.notekea.shell.data.chat

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.core.notes.domain.RelativePath

/**
 * Orders a plan's steps so what goes to the trash goes last, in one step: the system's trash cannot give an entry
 * back, so a plan that failed after trashing could not be taken back whole; a step that fails before it rolls back.
 */
internal object TrashLast {
    fun ordered(steps: List<ActionStep>): List<ActionStep> {
        // A trash a later step names stays in place: "trash Ideas, then make a new Ideas" needs the old one gone.
        val deferred = steps
            .indices
            .filter {
                steps[it].kind == ActionKind.TRASH
            }
            .filterNot { isNamedLater(steps, it) }
            .toSet()

        if (deferred.isEmpty()) return steps

        val trashed = deferred.flatMap { trashedBy(step = steps[it]) }

        // An entry inside a folder trashed too goes with the folder; trashing it again would fail.
        val outermost = trashed
            .distinct()
            .filterNot { path ->
                trashed.any {
                    it != path && path.isWithin(folder = it)
                }
            }

        val kept = steps.filterIndexed { index, _ ->
            index !in deferred
        }

        return kept + ActionStep(
            kind = ActionKind.TRASH,
            notes = outermost
        )
    }

    private fun isNamedLater(
        steps: List<ActionStep>,
        index: Int
    ): Boolean {
        val trashed = trashedBy(step = steps[index])

        return steps
            .drop(n = index + 1)
            .filter {
                it.kind != ActionKind.TRASH
            }
            .flatMap { namedBy(step = it) }
            .any { named ->
                trashed.any { it.overlaps(other = named) }
            }
    }

    private fun RelativePath.overlaps(other: RelativePath): Boolean =
        isWithin(folder = other) || other.isWithin(folder = this)

    private fun trashedBy(step: ActionStep): List<RelativePath> =
        listOfNotNull(step.folder).ifEmpty { step.notes }

    // The vault's root holds everything, so naming it says nothing about the trashed entry.
    private fun namedBy(step: ActionStep): List<RelativePath> {
        val drafts = step.content.map { it.path }
        val named = step.notes + drafts + listOfNotNull(step.folder, step.target, step.template)

        return named.filterNot { it.isRoot }
    }
}

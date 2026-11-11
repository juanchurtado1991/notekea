package app.notekea.chat.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * Checks a plan against the vault before anything is done (3.9): every note, folder and template it names must exist
 * (a note is found by its name, as a person writes it), a folder or a tag stands for its notes, at most [MAX_NOTES] of
 * them, and every step has what its action needs. The model only chose; the app decides what the names mean.
 */
class CheckPlanUseCase {
    operator fun invoke(
        steps: List<PlannedStep>,
        notes: Collection<NoteEntry>,
        openNote: RelativePath?
    ): Outcome<List<ActionStep>, ActionProblem> {
        val vault = VaultNames(
            notes = notes,
            openNote = openNote
        )

        val checked = steps.map { step ->
            when (val one = check(step = step, vault = vault)) {
                is Outcome.Success -> one.value
                is Outcome.Failure -> return one
            }
        }

        return Outcome.Success(value = checked)
    }

    private fun check(
        step: PlannedStep,
        vault: VaultNames
    ): Outcome<ActionStep, ActionProblem> {
        if (step.kind == ActionKind.REFUSE) {
            val reason = step[ActionField.TEXT].orEmpty()

            return Outcome.Failure(error = ActionProblem.Refused(reason = reason))
        }

        val missing = step.missing.firstOrNull()

        if (missing != null) {
            return Outcome.Failure(
                error = ActionProblem.Missing(
                    kind = step.kind,
                    field = missing
                )
            )
        }

        val targets = vault.targetsOf(step = step)
        if (targets is Outcome.Failure) return targets
        val found = (targets as Outcome.Success).value

        val isTooMany = found.notes.size > MAX_NOTES
        if (isTooMany) return Outcome.Failure(error = ActionProblem.TooManyNotes(found.notes.size))

        return Outcome.Success(
            value = ActionStep(
                kind = step.kind,
                notes = found.notes,
                folder = found.folder,
                target = found.target,
                template = found.template,
                title = step[ActionField.TITLE],
                text = step[ActionField.TEXT],
                property = step[ActionField.PROPERTY],
                value = step[ActionField.VALUE],
                tag = step[ActionField.TAG]?.removePrefix(prefix = "#"),
                due = step[ActionField.DUE],
                period = step[ActionField.PERIOD],
                panel = step[ActionField.PANEL],
                theme = step[ActionField.THEME],
                request = step[ActionField.REQUEST],
                url = step[ActionField.URL]
            )
        )
    }

    companion object {
        /** The most notes one step may cover ("tag every note in Recipes"), all listed on the card. */
        const val MAX_NOTES = 50
    }
}

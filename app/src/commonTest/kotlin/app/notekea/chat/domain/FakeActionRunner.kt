package app.notekea.chat.domain

import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.DonePlan
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteEntry

/** [ActionRunner] that keeps what it was asked: the steps [ran], the plans [undone], the [themes] set. */
class FakeActionRunner : ActionRunner {
    val ran = mutableListOf<List<ActionStep>>()
    val undone = mutableListOf<DonePlan>()
    val themes = mutableListOf<String>()
    var found = FoundNotes()
    var done = DonePlan(id = 1)

    override suspend fun find(
        vaultPath: String,
        step: ActionStep,
        notes: Collection<NoteEntry>
    ): Outcome<FoundNotes, ActionProblem> = Outcome.Success(value = found)

    override suspend fun run(
        vaultPath: String,
        steps: List<ActionStep>,
        notes: Collection<NoteEntry>
    ): Outcome<DonePlan, ActionProblem> {
        ran += steps

        return Outcome.Success(value = done)
    }

    override suspend fun setTheme(theme: String): Boolean {
        themes += theme

        return true
    }

    override suspend fun undo(
        vaultPath: String,
        done: DonePlan
    ): Outcome<Unit, ActionProblem> {
        undone += done

        return Outcome.Success(value = Unit)
    }
}

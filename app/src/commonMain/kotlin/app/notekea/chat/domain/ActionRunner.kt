package app.notekea.chat.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * Carries out checked steps with the use cases the app already has (3.9). The app composes it from the features (the
 * chat depends on none of them); the chat only asks. A plan that was done can be [undo]ne at once.
 */
interface ActionRunner {
    /** The notes or tasks a [ActionKind.FIND_NOTES], [ActionKind.SHOW_TASKS] or list step finds, as a table. */
    suspend fun find(
        vaultPath: String,
        step: ActionStep,
        notes: Collection<NoteEntry>
    ): Outcome<FoundNotes, ActionProblem>

    /** Writes or organizes the vault as [steps] say, in order, stopping at the first that cannot be done. */
    suspend fun run(
        vaultPath: String,
        steps: List<ActionStep>,
        notes: Collection<NoteEntry>
    ): Outcome<DonePlan, ActionProblem>

    /** Switches the app to the color theme named [theme] (its id or its name); false when there is no such theme. */
    suspend fun setTheme(theme: String): Boolean

    /** Takes back what [done] changed, newest first; a note edited since is left as it is. */
    suspend fun undo(
        vaultPath: String,
        done: DonePlan
    ): Outcome<Unit, ActionProblem>
}

package app.notekea.shell.data.chat

import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NoteScanner
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.links.UpdateLinksAfterRenameUseCase
import app.notekea.tree.domain.Trash

/**
 * The plan's steps that move what is in the vault: renames and moves, which update the links to what moved as the
 * tree does and are taken back by Undo, and trashing, which is not (what went to the system's trash stays there).
 */
internal class EntryMoves(
    private val noteFileOperations: NoteFileOperations,
    private val noteScanner: NoteScanner,
    private val renameEntry: RenameEntryUseCase,
    private val updateLinksAfterRename: UpdateLinksAfterRenameUseCase,
    private val trash: Trash
) {
    suspend fun rename(
        vaultPath: String,
        step: ActionStep,
        journal: ActionJournal
    ): Outcome<Unit, ActionProblem> {
        val from = step.folder ?: step.notes.singleOrNull() ?: return Outcome.Success(value = Unit)
        val newName = step.title.orEmpty()
        val notesBefore = notesOf(vaultPath = vaultPath)

        val renamed =
            when (val result = renameEntry(vaultPath = vaultPath, path = from, newName = newName)) {
                is Outcome.Success -> result.value
                is Outcome.Failure -> return Outcome.Failure(ActionProblem.UnknownNote(from.value))
            }

        updateLinksAfterRename(
            vaultPath = vaultPath,
            notesBefore = notesBefore,
            from = from,
            to = renamed
        )

        journal.moved += from to renamed

        journal.undoSteps += { path ->
            movedBack(
                vaultPath = path,
                from = renamed,
                to = from
            )
        }

        return Outcome.Success(value = Unit)
    }

    suspend fun move(
        vaultPath: String,
        step: ActionStep,
        journal: ActionJournal
    ): Outcome<Unit, ActionProblem> {
        val folder = step.target ?: return Outcome.Success(value = Unit)

        // "Move it to .." would take the note out of the vault.
        if (!folder.isInside) return Outcome.Failure(ActionProblem.UnknownFolder(folder.value))

        noteFileOperations.createFolder(
            vaultPath = vaultPath,
            path = folder
        )

        listOfNotNull(step.folder).ifEmpty { step.notes }.forEach { from ->
            val moved = moved(
                vaultPath = vaultPath,
                from = from,
                to = folder.child(name = from.name),
                journal = journal
            )

            if (moved is Outcome.Failure) return moved
        }

        return Outcome.Success(value = Unit)
    }

    // One entry moved; a name in the way fails the plan, as the move said done and moved nothing (A11).
    private suspend fun moved(
        vaultPath: String,
        from: RelativePath,
        to: RelativePath,
        journal: ActionJournal
    ): Outcome<Unit, ActionProblem> {
        if (from == to) return Outcome.Success(value = Unit)

        val isTaken = noteFileOperations.exists(
            vaultPath = vaultPath,
            path = to
        )

        if (isTaken) return Outcome.Failure(error = ActionProblem.Exists(path = to))

        val notesBefore = notesOf(vaultPath = vaultPath)

        val moved = noteFileOperations.move(
            vaultPath = vaultPath,
            from = from,
            to = to
        )

        if (moved is Outcome.Failure) return Outcome.Failure(ActionProblem.UnknownNote(from.value))

        updateLinksAfterRename(
            vaultPath = vaultPath,
            notesBefore = notesBefore,
            from = from,
            to = to
        )

        journal.moved += from to to

        journal.undoSteps += { path ->
            movedBack(
                vaultPath = path,
                from = to,
                to = from
            )
        }

        return Outcome.Success(value = Unit)
    }

    // A move back that fails is a step of Undo not done, and counted as one.
    private suspend fun movedBack(
        vaultPath: String,
        from: RelativePath,
        to: RelativePath
    ) {
        val notesBefore = notesOf(vaultPath = vaultPath)

        val moved = noteFileOperations.move(
            vaultPath = vaultPath,
            from = from,
            to = to
        )

        if (moved is Outcome.Failure) throw UndoFailed()

        updateLinksAfterRename(
            vaultPath = vaultPath,
            notesBefore = notesBefore,
            from = from,
            to = to
        )
    }

    // A note the trash refuses fails the plan, as it was said trashed (A11).
    suspend fun trashed(
        vaultPath: String,
        step: ActionStep,
        journal: ActionJournal
    ): Outcome<Unit, ActionProblem> {
        listOfNotNull(step.folder).ifEmpty { step.notes }.forEach { path ->
            val moved = trash.moveToTrash(
                vaultPath = vaultPath,
                path = path
            )

            val refused = ActionProblem.UnknownNote(name = path.value)
            if (moved is Outcome.Failure) return Outcome.Failure(error = refused)

            journal.trashed += path
        }

        return Outcome.Success(value = Unit)
    }

    private suspend fun notesOf(vaultPath: String): Set<RelativePath> =
        when (val listing = noteScanner.scan(vaultPath = vaultPath)) {
            is Outcome.Success ->
                listing
                    .value
                    .files
                    .mapTo(destination = HashSet()) { it.path }

            is Outcome.Failure -> emptySet()
        }
}

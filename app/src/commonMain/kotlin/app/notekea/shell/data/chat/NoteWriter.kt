package app.notekea.shell.data.chat

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.LineEdit
import app.notekea.chat.domain.plan.NoteFileName
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.core.domain.Outcome
import app.notekea.core.memory.domain.MemoryEdit
import app.notekea.core.memory.domain.MemorySection
import app.notekea.core.notes.domain.NewNoteDefaults
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.NoteRewrite
import app.notekea.tree.domain.ApplyTemplateUseCase
import app.notekea.tree.domain.Trash

/** Writes notes for one plan, each change kept in [journal] to be taken back. */
internal class NoteWriter(
    private val noteContentStore: NoteContentStore,
    private val noteFileOperations: NoteFileOperations,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase,
    private val applyTemplate: ApplyTemplateUseCase,
    private val trash: Trash,
    private val finder: VaultActionFinder,
    private val vaultPath: String,
    val journal: ActionJournal,
    private val notes: Collection<NoteEntry>,
    private val defaults: NewNoteDefaults? = null
) {
    suspend fun createNote(step: ActionStep): Outcome<Unit, ActionProblem> {
        val draft = step.content.firstOrNull()

        val path = draft?.path ?: (step.folder ?: RelativePath.ROOT).child(
            name = NoteFileName.of(title = step.title) + RelativePath.MARKDOWN_EXTENSION
        )

        val text = draft?.text ?: "# ${step.title}\n"

        val created = create(
            path = path,
            text = defaults?.on(text = text) ?: text
        )

        if (created is Outcome.Failure) return created

        val template = step.template ?: return created

        applyTemplate(
            vaultPath = vaultPath,
            note = path,
            template = template
        )

        // Undo compares with the text the template made, or a note made from one stayed after Undo (A11).
        noteContentStore
            .read(
                vaultPath = vaultPath,
                path = path
            )
            ?.let { journal.madeTexts[path] = it }

        return created
    }

    suspend fun createDrafts(step: ActionStep): Outcome<Unit, ActionProblem> {
        step.content.forEach { draft ->
            val created = create(
                path = draft.path,
                text = draft.text
            )

            if (created is Outcome.Failure) return created
        }

        return Outcome.Success(value = Unit)
    }

    suspend fun createFolder(folder: RelativePath): Outcome<Unit, ActionProblem> {
        // A folder named "../.." would be made outside the vault.
        val outside = ActionProblem.UnknownFolder(folder.value)
        if (!folder.isInside) return Outcome.Failure(error = outside)

        val isNew = !noteFileOperations.exists(
            vaultPath = vaultPath,
            path = folder
        )

        noteFileOperations.createFolder(
            vaultPath = vaultPath,
            path = folder
        )

        if (isNew) journal.createdFolders += folder

        return Outcome.Success(value = Unit)
    }

    private suspend fun create(
        path: RelativePath,
        text: String
    ): Outcome<Unit, ActionProblem> {
        if (!path.isInside) return Outcome.Failure(error = ActionProblem.UnknownFolder(path.value))

        path
            .parent
            ?.takeUnless { it.isRoot }
            ?.let { createFolder(folder = it) }

        val isTaken = noteFileOperations.exists(
            vaultPath = vaultPath,
            path = path
        )

        if (isTaken) return Outcome.Failure(error = ActionProblem.Exists(path = path))

        val written = noteContentStore.write(
            vaultPath = vaultPath,
            path = path,
            content = text
        )

        val unwritable = ActionProblem.UnknownFolder(path.value)
        if (written is Outcome.Failure) return Outcome.Failure(unwritable)

        journal.created += path
        journal.madeTexts[path] = text

        // Taken back only as it was written: a note the person changed since is theirs, and stays.
        journal.undoSteps += { vault ->
            val now = noteContentStore.read(
                vaultPath = vault,
                path = path
            )

            if (now == journal.madeTexts[path]) {
                val trashed = trash.moveToTrash(
                    vaultPath = vault,
                    path = path
                )

                if (trashed is Outcome.Failure) throw UndoFailed()
            }
        }

        return Outcome.Success(value = Unit)
    }

    /** Adds [text] as a paragraph at the end of each of [notes]. */
    suspend fun append(
        notes: List<RelativePath>,
        text: String
    ): Outcome<Unit, ActionProblem> {
        if (notes.isEmpty()) return Outcome.Failure(error = ActionProblem.UnknownNote(name = ""))

        notes.forEach { path ->
            val before = noteContentStore.read(
                vaultPath = vaultPath,
                path = path
            ) ?: return Outcome.Failure(ActionProblem.UnknownNote(path.nameWithoutExtension))

            val after = AppendedText.of(
                before = before,
                added = text
            )

            rewrite(
                rewrite = NoteRewrite(
                    path = path,
                    original = before,
                    updated = after,
                    linkCount = 0
                )
            )
        }

        return Outcome.Success(value = Unit)
    }

    /** A step of no kind above: the chat's memory written (`docs/agent-memory.md`); else shown, not written. */
    suspend fun otherwise(step: ActionStep): Outcome<Unit, ActionProblem> =
        if (step.kind.isLoopOnly) {
            remembered(step = step)
        } else {
            Outcome.Success(value = Unit)
        }

    /** A line put into the chat's memory, or taken out of it (`docs/agent-memory.md`); the note made when missing. */
    suspend fun remembered(step: ActionStep): Outcome<Unit, ActionProblem> {
        val path = VaultGuide.MEMORY
        val line = step.text.orEmpty()

        val before = noteContentStore.read(
            vaultPath = vaultPath,
            path = path
        )

        val after = if (step.kind == ActionKind.FORGET) {
            MemoryEdit.forgotten(
                note = before.orEmpty(),
                line = line
            )
        } else {
            MemoryEdit.remembered(
                note = before.orEmpty(),
                line = line,
                section = MemorySection.of(heading = step.property.orEmpty()),
                origin = step.value.orEmpty()
            )
        }

        if (before == null) {
            return create(
                path = path,
                text = after
            )
        }

        if (after != before) {
            rewrite(
                rewrite = NoteRewrite(
                    path = path,
                    original = before,
                    updated = after,
                    linkCount = 0
                )
            )
        }

        return Outcome.Success(value = Unit)
    }

    // The note's tasks as they are now, not as the plan's index saw them: a task added before is found (T15).
    suspend fun completeTask(step: ActionStep): Outcome<Unit, ActionProblem> {
        val path = step.notes.firstOrNull()

        val text = path?.let {
            noteContentStore.read(
                vaultPath = vaultPath,
                path = it
            )
        } ?: return Outcome.Failure(ActionProblem.UnknownTask(step.text.orEmpty()))

        val done = finder.completeTask(
            vaultPath = vaultPath,
            step = step,
            tasks = CurrentTasks.of(text = text)
        )

        return when (done) {
            is Outcome.Success -> {
                record(rewrite = done.value)

                Outcome.Success(value = Unit)
            }

            is Outcome.Failure -> Outcome.Failure(error = done.error)
        }
    }

    // The line found again in the note as it is now, by its words: one the person changed since the card is still
    // found, and none or several alike fail the plan, not a guess (4.2). Undo writes the line back.
    suspend fun changeLine(step: ActionStep): Outcome<Unit, ActionProblem> {
        val unknown = Outcome.Failure(error = ActionProblem.UnknownLine(text = step.line.orEmpty()))
        val path = step.notes.firstOrNull() ?: return unknown

        val before = noteContentStore.read(
            vaultPath = vaultPath,
            path = path
        ) ?: return unknown

        val after = LineEdit.changed(
            text = before,
            old = step.line.orEmpty(),
            new = step.text.orEmpty()
        ) ?: return unknown

        rewrite(
            rewrite = NoteRewrite(
                path = path,
                original = before,
                updated = after,
                linkCount = 0
            )
        )

        return Outcome.Success(value = Unit)
    }

    // A note that cannot be read fails the plan: a tag on a note moved away was written nowhere, and said done (A6).
    suspend fun setProperty(step: ActionStep): Outcome<Unit, ActionProblem> {
        step.notes.forEach { path ->
            noteContentStore.read(
                vaultPath = vaultPath,
                path = path
            ) ?: return Outcome.Failure(ActionProblem.UnknownNote(name = path.nameWithoutExtension))

            finder.setProperty(
                vaultPath = vaultPath,
                step = step,
                path = path,
                notes = notes
            )?.let { record(rewrite = it) }
        }

        return Outcome.Success(value = Unit)
    }

    private suspend fun rewrite(rewrite: NoteRewrite) {
        val result = applyLinkUpdates(
            vaultPath = vaultPath,
            rewrites = listOf(rewrite)
        )

        if (rewrite.path in result.updated) record(rewrite = rewrite)
    }

    /** Keeps a change already written, so Undo writes it back unless the note changed again. */
    private fun record(rewrite: NoteRewrite) {
        journal.changed += rewrite.path

        journal.undoSteps += { vault ->
            applyLinkUpdates(
                vaultPath = vault,
                rewrites = listOf(rewrite.reversed())
            )
        }
    }
}

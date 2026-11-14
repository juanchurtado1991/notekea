package app.notekea.shell.data.chat

import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.core.domain.Outcome
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
    private val notes: Collection<NoteEntry>
) {
    suspend fun createNote(step: ActionStep): Outcome<Unit, ActionProblem> {
        val draft = step.content.firstOrNull()

        val path =
            draft?.path ?: (step.folder ?: RelativePath.ROOT).child(name = "${fileName(step.title)}.md")

        val created = create(
            path = path,
            text = draft?.text ?: "# ${step.title}\n"
        )

        if (created is Outcome.Failure) return created

        val template = step.template ?: return created

        applyTemplate(
            vaultPath = vaultPath,
            note = path,
            template = template
        )

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

        // Taken back only as it was written: a note the person changed since is theirs, and stays.
        journal.undoSteps += { vault ->
            val now = noteContentStore.read(
                vaultPath = vault,
                path = path
            )

            if (now == text) {
                trash.moveToTrash(
                    vaultPath = vault,
                    path = path
                )
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

            val after = before.trimEnd() + "\n\n" + text.trim() + "\n"

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

    suspend fun completeTask(step: ActionStep): Outcome<Unit, ActionProblem> {
        val done = finder.completeTask(
            vaultPath = vaultPath,
            step = step,
            notes = notes
        )
            ?: return Outcome.Failure(ActionProblem.UnknownTask(step.text.orEmpty()))

        record(rewrite = done)

        return Outcome.Success(value = Unit)
    }

    suspend fun setProperty(step: ActionStep): Outcome<Unit, ActionProblem> {
        step.notes.forEach { path ->
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

    /** [title] as a file's name: the characters no system allows in one are left out. */
    private fun fileName(title: String?): String = title
        .orEmpty()
        .replace(FORBIDDEN, "")
        .trim()
        .ifEmpty { UNTITLED }

    private companion object {
        const val UNTITLED = "Untitled"
        val FORBIDDEN = Regex(pattern = "[\\\\/:*?\"<>|#^\\[\\]]")
    }
}

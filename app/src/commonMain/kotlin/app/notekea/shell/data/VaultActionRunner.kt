package app.notekea.shell.data

import app.notekea.chat.domain.ActionKind
import app.notekea.chat.domain.ActionProblem
import app.notekea.chat.domain.ActionRunner
import app.notekea.chat.domain.ActionStep
import app.notekea.chat.domain.DonePlan
import app.notekea.chat.domain.FoundNotes
import app.notekea.core.appearance.domain.AppearanceStore
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.ThemeMode
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NoteRewrite
import app.notekea.core.notes.domain.NoteScanner
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.UpdateLinksAfterRenameUseCase
import app.notekea.tree.domain.ApplyTemplateUseCase
import app.notekea.tree.domain.BookmarkStore
import app.notekea.tree.domain.OpenDailyNoteUseCase
import app.notekea.tree.domain.Trash
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The chat's [ActionRunner], composed from the features' use cases (3.9): writes go through [NoteRewrite]s, which
 * leave a note edited meanwhile alone; renames and moves update the links to what moved, as the tree does. Every plan
 * keeps a journal of what it did, so Undo takes it back; what went to the system's trash stays there.
 */
class VaultActionRunner(
    private val noteContentStore: NoteContentStore,
    private val noteFileOperations: NoteFileOperations,
    private val noteScanner: NoteScanner,
    private val renameEntry: RenameEntryUseCase,
    private val updateLinksAfterRename: UpdateLinksAfterRenameUseCase,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase,
    private val applyTemplate: ApplyTemplateUseCase,
    private val openDailyNote: OpenDailyNoteUseCase,
    private val bookmarkStore: BookmarkStore,
    private val trash: Trash,
    private val finder: VaultActionFinder,
    private val appearanceStore: AppearanceStore
) : ActionRunner {
    private val lock = Mutex()
    private val journals = mutableMapOf<Long, ActionJournal>()
    private var nextId = 0L

    override suspend fun find(
        vaultPath: String,
        step: ActionStep,
        notes: Collection<NoteEntry>
    ): Outcome<FoundNotes, ActionProblem> = finder.find(
        vaultPath = vaultPath,
        step = step,
        notes = notes
    )

    override suspend fun run(
        vaultPath: String,
        steps: List<ActionStep>,
        notes: Collection<NoteEntry>
    ): Outcome<DonePlan, ActionProblem> = lock.withLock {
        val journal = ActionJournal()

        val writer = NoteWriter(
            noteContentStore = noteContentStore,
            noteFileOperations = noteFileOperations,
            applyLinkUpdates = applyLinkUpdates,
            applyTemplate = applyTemplate,
            trash = trash,
            finder = finder,
            vaultPath = vaultPath,
            journal = journal,
            notes = notes
        )

        steps.forEach { step ->
            val done = runStep(
                vaultPath = vaultPath,
                step = step,
                writer = writer
            )

            // A plan is done whole or not at all: what it did before the step that failed is taken back.
            if (done is Outcome.Failure) {
                journal
                    .undoSteps
                    .asReversed()
                    .forEach { it(vaultPath) }

                return@withLock done
            }
        }

        val id = nextId++
        journals[id] = journal

        // Only the latest plans can be undone: a journal holds what the notes were, so all of them would grow with
        // the session (memory leak hunt, 2026-11-11).
        journals
            .keys
            .sorted()
            .dropLast(n = MAX_UNDOABLE)
            .forEach { journals.remove(key = it) }

        Outcome.Success(value = journal.plan(id = id))
    }

    override suspend fun setTheme(theme: String): Boolean {
        val wanted = theme
            .trim()
            .lowercase()
            .replace(
                oldChar = ' ',
                newChar = '_'
            )

        val appearance = appearanceStore.observe().first()

        // "Dark", "oscuro", "light mode": the app's mode, not a color theme.
        MODES
            .entries
            .firstOrNull { (_, words) ->
                words.any {
                    it in wanted
                }
            }
            ?.let { (mode, _) ->
                appearanceStore.save(appearance = appearance.copy(mode = mode))

                return true
            }

        val found = ColorTheme.entries.firstOrNull {
            it.id == wanted || it.id.endsWith(suffix = wanted)
        } ?: return false

        appearanceStore.save(appearance = appearance.copy(theme = found))

        return true
    }

    override suspend fun undo(
        vaultPath: String,
        done: DonePlan
    ): Outcome<Unit, ActionProblem> = lock.withLock {
        val journal = journals.remove(done.id) ?: return@withLock Outcome.Success(value = Unit)

        journal
            .undoSteps
            .asReversed()
            .forEach { it(vaultPath) }

        Outcome.Success(value = Unit)
    }

    private suspend fun runStep(
        vaultPath: String,
        step: ActionStep,
        writer: NoteWriter
    ): Outcome<Unit, ActionProblem> = when (step.kind) {
        ActionKind.CREATE_NOTE -> writer.createNote(step = step)

        ActionKind.WRITE_FROM_NOTES, ActionKind.NOTES_FROM_TEXT, ActionKind.CLIP_URL ->
            writer.createDrafts(step = step)

        ActionKind.MAKE_CARDS -> writer.append(
            notes = step.notes,
            text = step.text.orEmpty()
        )

        ActionKind.APPEND_TEXT -> writer.append(
            notes = notesOrToday(vaultPath, step, writer.journal),
            text = step.text.orEmpty()
        )

        ActionKind.ADD_TASK -> writer.append(
            notes = notesOrToday(vaultPath, step, writer.journal),
            text = taskLine(step = step)
        )

        ActionKind.COMPLETE_TASK -> writer.completeTask(step = step)

        ActionKind.SET_PROPERTY, ActionKind.ADD_TAG -> writer.setProperty(step = step)

        ActionKind.LINK_NOTES -> linkNotes(
            step = step,
            writer = writer
        )

        ActionKind.BOOKMARK -> bookmark(
            vaultPath = vaultPath,
            step = step,
            journal = writer.journal
        )

        ActionKind.CREATE_FOLDER -> writer.createFolder(folder = step.folder ?: RelativePath.ROOT)

        ActionKind.RENAME -> rename(
            vaultPath = vaultPath,
            step = step,
            journal = writer.journal
        )

        ActionKind.MOVE -> move(
            vaultPath = vaultPath,
            step = step,
            journal = writer.journal
        )

        ActionKind.TRASH -> trashed(
            vaultPath = vaultPath,
            step = step,
            journal = writer.journal
        )

        ActionKind.SET_THEME -> {
            setTheme(theme = step.theme.orEmpty())
            Outcome.Success(value = Unit)
        }

        else -> Outcome.Success(value = Unit)
    }

    /** The step's notes, or today's daily note (made if it is not there yet) when it names none. */
    private suspend fun notesOrToday(
        vaultPath: String,
        step: ActionStep,
        journal: ActionJournal
    ): List<RelativePath> {
        if (step.notes.isNotEmpty()) return step.notes

        val today =
            (openDailyNote(vaultPath = vaultPath) as? Outcome.Success)?.value ?: return emptyList()

        // A daily note the step had to make goes with Undo too.
        if (today.isNew) {
            journal.created += today.path

            journal.undoSteps += { vault ->
                trash.moveToTrash(
                    vaultPath = vault,
                    path = today.path
                )
            }
        }

        return listOf(today.path)
    }

    private fun taskLine(step: ActionStep): String {
        val due = step
            .due
            ?.takeIf { DATE.matches(input = it) }
            ?.let { " 📅 $it" }
            .orEmpty()

        return "- [ ] ${step.text.orEmpty().removePrefix(prefix = "- [ ] ")}$due"
    }

    private suspend fun linkNotes(
        step: ActionStep,
        writer: NoteWriter
    ): Outcome<Unit, ActionProblem> {
        val target = step.target ?: return Outcome.Success(value = Unit)

        return writer.append(
            notes = step.notes,
            text = "[[${target.nameWithoutExtension}]]"
        )
    }

    private suspend fun bookmark(
        vaultPath: String,
        step: ActionStep,
        journal: ActionJournal
    ): Outcome<Unit, ActionProblem> {
        val before = bookmarkStore.read(vaultPath = vaultPath)

        val wanted = (step.notes + listOfNotNull(step.folder)).filterNot {
            it in before
        }

        bookmarkStore.write(
            vaultPath = vaultPath,
            bookmarks = wanted
                .fold(before) { marks, path -> marks.toggled(path) }
        )

        journal.undoSteps += { path ->
            val now = bookmarkStore.read(vaultPath = path)

            val added = wanted.filter {
                it in now
            }

            bookmarkStore.write(
                vaultPath = path,
                bookmarks = added.fold(now) { marks, mark -> marks.toggled(mark) }
            )
        }

        return Outcome.Success(value = Unit)
    }

    private suspend fun rename(
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

    private suspend fun move(
        vaultPath: String,
        step: ActionStep,
        journal: ActionJournal
    ): Outcome<Unit, ActionProblem> {
        val folder = step.target ?: return Outcome.Success(value = Unit)

        noteFileOperations.createFolder(
            vaultPath = vaultPath,
            path = folder
        )

        val entries = listOfNotNull(step.folder).ifEmpty { step.notes }

        entries.forEach { from ->
            val to = folder.child(name = from.name)
            val isPlaced = from == to || noteFileOperations.exists(vaultPath = vaultPath, path = to)
            if (isPlaced) return@forEach

            val notesBefore = notesOf(vaultPath = vaultPath)

            val moved = noteFileOperations.move(
                vaultPath = vaultPath,
                from = from,
                to = to
            )

            val unmovable = ActionProblem.UnknownNote(from.value)
            if (moved is Outcome.Failure) return Outcome.Failure(unmovable)

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
        }

        return Outcome.Success(value = Unit)
    }

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

        if (moved is Outcome.Success) {
            updateLinksAfterRename(
                vaultPath = vaultPath,
                notesBefore = notesBefore,
                from = from,
                to = to
            )
        }
    }

    private suspend fun trashed(
        vaultPath: String,
        step: ActionStep,
        journal: ActionJournal
    ): Outcome<Unit, ActionProblem> {
        listOfNotNull(step.folder).ifEmpty { step.notes }.forEach { path ->
            val moved = trash.moveToTrash(
                vaultPath = vaultPath,
                path = path
            )

            if (moved is Outcome.Success) journal.trashed += path
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

    private companion object {
        const val MAX_UNDOABLE = 20
        val DATE = Regex(pattern = "\\d{4}-\\d{2}-\\d{2}")

        val MODES = mapOf(
            ThemeMode.DARK to listOf("dark", "oscuro", "oscura", "noche", "night", "negro"),
            ThemeMode.LIGHT to listOf("light", "claro", "clara", "día", "dia", "blanco"),
            ThemeMode.SYSTEM to listOf("system", "sistema", "automatic", "automático", "automatico")
        )
    }
}

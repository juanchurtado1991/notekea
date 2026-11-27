package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.NewNotePlace
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.ChatEffect
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import kotlin.time.Clock

/**
 * The chat's side of actions (3.8, 3.9), for [ChatViewModel]: the card's Do it, Cancel and Undo, and what a request
 * is worked on with.
 */
internal class ChatActions(
    private val vaultPath: String,
    private val actionRunner: ActionRunner,
    private val localCalendar: LocalCalendar,
    private val clock: Clock,
    private val host: ChatHost
) {
    private val undoing = mutableSetOf<Long>()

    /** Do it: the card's steps are carried out; the first note made opens. */
    suspend fun confirm(
        entry: ChatEntry,
        notes: Collection<NoteEntry>
    ) {
        val action = entry.action?.takeIf {
            it.status == ActionStatus.PROPOSED
        } ?: return

        host.update(entry.id) { it.copy(action = action.copy(status = ActionStatus.RUNNING)) }

        when (
            val done = actionRunner.run(vaultPath = vaultPath, steps = action.steps, notes = notes)
        ) {
            is Outcome.Success -> {
                host
                    .update(entry.id) {
                        it.copy(
                            action = action.copy(
                                status = ActionStatus.DONE,
                                done = done.value
                            )
                        )
                    }

                done
                    .value
                    .opened
                    ?.let { host.emit(ChatEffect.Open(note = it)) }

                action
                    .steps
                    .filter {
                        !it.kind.isConfirmed && it.kind != ActionKind.SET_THEME
                    }
                    .forEach { host.emit(ChatEffect.Show(step = it)) }
            }

            is Outcome.Failure ->
                host
                    .update(entry.id) {
                        it.copy(
                            action = action.copy(
                                status = ActionStatus.FAILED,
                                problem = done.error
                            )
                        )
                    }
        }
    }

    /** The card's step [step] creates its note in [folder], while the card still waits for Do it. */
    fun place(intent: ChatIntent.OnActionPlace) {
        val entry = entryOf(id = intent.id)

        val action = entry?.action?.takeIf {
            it.status == ActionStatus.PROPOSED
        } ?: return

        val placed = action.steps.mapIndexed { index, planned ->
            val isPlaced = index == intent.step && NewNotePlace.isChosenFor(step = planned)

            if (isPlaced) {
                NewNotePlace.placed(
                    step = planned,
                    folder = intent.folder
                )
            } else {
                planned
            }
        }

        host.update(entry.id) { it.copy(action = action.copy(steps = placed)) }
    }

    /** The vault now holds [notes]: the state lists the folders a new note can go to; returns them as given. */
    fun holding(notes: Collection<NoteEntry>): Collection<NoteEntry> {
        host.reduce { it.holding(notes) }

        return notes
    }

    fun cancel(entry: ChatEntry) {
        val action = entry.action?.takeIf {
            it.status == ActionStatus.PROPOSED
        } ?: return

        host.update(entry.id) { it.copy(action = action.copy(status = ActionStatus.CANCELLED)) }
    }

    // The code review of 2026-11-22: a second Undo while the first ran undid the plan twice, and the
    // card it had undone said it failed. The card is read as it is now, and one undo of it runs.
    suspend fun undo(entry: ChatEntry) {
        val action = entryOf(id = entry.id)?.action ?: return

        val done = action.done?.takeIf {
            action.status == ActionStatus.DONE
        } ?: return

        if (!undoing.add(element = entry.id)) return

        val undone = try {
            actionRunner.undo(
                vaultPath = vaultPath,
                done = done
            )
        } finally {
            undoing -= entry.id
        }

        // Only what was taken back is said to be: an undo that could not run says so, on the card.
        val after = when (undone) {
            is Outcome.Success -> action.copy(status = ActionStatus.UNDONE)

            is Outcome.Failure -> action.copy(
                status = ActionStatus.FAILED,
                problem = undone.error
            )
        }

        host.update(entry.id) { it.copy(action = after) }
    }

    private fun entryOf(id: Long): ChatEntry? = host
        .state
        .value
        .entries
        .firstOrNull {
            it.id == id
        }

    /** Today's date as the model reads it, YYYY-MM-DD. */
    fun today(): String = CalendarDate.today(
        localCalendar = localCalendar,
        clock = clock
    ).iso

    /** Today for the talk, with what changed in [notes]: "2026-11-11. Notes changed today: none. Last changed: …". */
    fun todayIn(notes: Collection<NoteEntry>): String {
        val context = contextOf(
            request = "",
            notes = notes,
            openNote = null
        )

        val changed = context
            .changedToday
            .joinToString()
            .ifEmpty { "none" }

        val lastAt = notes
            .mapNotNull { it.file.modifiedAt }
            .maxOrNull()
            ?.let {
                localCalendar
                    .momentOf(instant = it)
                    .date
                    .iso
            }

        val last = context
            .latestNote
            ?.let { " The last changed note is $it, changed on $lastAt." }
            .orEmpty()

        return "${context.today}. Notes changed today: $changed.$last"
    }

    /** The note changed last in [notes] and those changed today, by their paths without the extension. */
    private fun changesIn(notes: Collection<NoteEntry>): Pair<String?, List<String>> {
        val latest = notes
            .filter {
                it.file.modifiedAt != null
            }
            .maxByOrNull {
                it.file.modifiedAt!!
            }
            ?.file
            ?.path
            ?.bare()

        val day = CalendarDate.today(
            localCalendar = localCalendar,
            clock = clock
        )

        val changedToday = notes
            .filter { note ->
                note
                    .file
                    .modifiedAt
                    ?.let {
                        localCalendar.momentOf(instant = it).date == day
                    } == true
            }
            .map {
                it
                    .file
                    .path
                    .bare()
            }

        return latest to changedToday
    }

    /** What the plan, or agent mode, may name in the vault: its names, the closest to [request] first. */
    fun contextOf(
        request: String,
        notes: Collection<NoteEntry>,
        openNote: RelativePath?
    ): ActionContext {
        val words = request
            .lowercase()
            .split(WORD_BREAK)
            .filter {
                it.length > 2
            }
            .toSet()

        val paths = notes.map { it.file.path }

        val names = paths
            .sortedByDescending { path ->
                words.count {
                    it in path.value.lowercase()
                }
            }
            .map { it.bare() }

        val (latest, changedToday) = changesIn(notes = notes)

        return ActionContext(
            today = today(),
            working = host.working?.described(),
            changedToday = changedToday,
            openNote = openNote?.bare(),
            latestNote = latest,
            folders = ActionContext.foldersOf(paths = paths),
            notes = names,
            properties = notes
                .flatMap { note ->
                    note
                        .metadata
                        .properties
                        .map { it.key }
                }
                .distinct()
                .sorted(),
            tags = notes
                .flatMap {
                    it.metadata.tags + it.metadata.textTags
                }
                .distinct()
                .sorted(),
            themes = ColorTheme.entries.map {
                it.id.replace(
                    oldChar = '_',
                    newChar = ' '
                )
            } + MODE_THEMES
        )
    }

    private companion object {
        val WORD_BREAK = Regex(pattern = "[^\\p{L}\\p{N}]+")

        // The app's light or dark mode, which set_theme also takes.
        val MODE_THEMES = listOf("dark", "light", "system")
    }
}

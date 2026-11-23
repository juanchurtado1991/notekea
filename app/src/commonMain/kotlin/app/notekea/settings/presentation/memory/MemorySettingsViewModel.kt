package app.notekea.settings.presentation.memory

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.memory.domain.MemoryEdit
import app.notekea.core.memory.domain.MemoryField
import app.notekea.core.memory.domain.MemoryNote
import app.notekea.core.notes.domain.AttachmentStore
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.preferences.domain.AppPreferencesStore
import app.notekea.core.presentation.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

/**
 * Settings → Chat memory (`docs/agent-memory.md`, "How the person sets it up") for the vault at [vaultPath]: the
 * memory read from `Notekea/Memory.md` and written back to it, so the note stays what is kept and this page only
 * shows it. A field is saved a moment after it is typed; Forget everything moves the note to the vault's trash.
 */
class MemorySettingsViewModel(
    private val noteContentStore: NoteContentStore,
    private val noteFileOperations: NoteFileOperations,
    private val attachmentStore: AttachmentStore,
    private val preferencesStore: AppPreferencesStore,
    private val localCalendar: LocalCalendar,
    private val clock: Clock,
    private val vaultPath: String?
) : BaseViewModel<MemorySettingsState, MemorySettingsIntent, Nothing>(
    initialState = MemorySettingsState(hasVault = vaultPath != null)
) {

    // Each field waits for its own pause, so typing in one never drops another's write (as Settings → Files does).
    private val saving = mutableMapOf<MemoryField, Job>()

    init {
        launchGuarded(onError = ::logError) { load() }
    }

    override fun handleIntent(intent: MemorySettingsIntent) {
        when (intent) {
            MemorySettingsIntent.OnShown -> launchGuarded(onError = ::logError) { load() }

            MemorySettingsIntent.OnToggle -> launchGuarded(onError = ::logError) {
                val preferences = preferencesStore
                    .observe()
                    .first()

                val toggled = preferences.copy(chatMemory = !preferences.chatMemory)
                preferencesStore.save(preferences = toggled)
                reduce { it.copy(isOn = !preferences.chatMemory) }
            }

            is MemorySettingsIntent.OnFieldChange -> changeField(
                field = intent.field,
                value = intent.value
            )

            is MemorySettingsIntent.OnForget -> edit {
                MemoryEdit.forgotten(
                    note = it,
                    line = intent.text
                )
            }

            MemorySettingsIntent.OnForgetAll -> reduce { it.copy(isForgetting = true) }

            MemorySettingsIntent.OnForgetAllCancel -> reduce { it.copy(isForgetting = false) }

            MemorySettingsIntent.OnForgetAllConfirm -> launchGuarded(onError = ::logError) {
                forgetAll()
            }
        }
    }

    private suspend fun load() {
        val isOn = preferencesStore
            .observe()
            .first()
            .chatMemory

        val vault = vaultPath ?: return reduce { it.copy(isOn = isOn) }

        val note = noteContentStore.read(
            vaultPath = vault,
            path = MemoryNote.PATH
        ).orEmpty()

        val folders = attachmentStore.folders(vaultPath = vault)

        val fields = MemoryField.entries.associateWith {
            MemoryEdit.fieldOf(
                note = note,
                section = it.section,
                prefix = it.prefix
            )
        }

        val learnt = MemoryNote.linesOf(text = note).filterNot { line ->
            MemoryField.entries.any {
                line.section == it.section && line.text.startsWith(prefix = it.prefix)
            }
        }

        reduce {
            it.copy(
                isOn = isOn,
                fields = fields,
                lines = learnt,
                folders = folders
                    .map { folder -> folder.value }
                    .filterNot { folder -> folder.startsWith(prefix = ".") }
                    .sorted()
            )
        }
    }

    private fun changeField(
        field: MemoryField,
        value: String
    ) {
        reduce {
            it.copy(
                fields = it.fields + (field to value),
                hasFailed = false
            )
        }

        saving[field]?.cancel()

        saving[field] = launchGuarded(onError = ::logError) {
            delay(duration = PAUSE)

            write { note ->
                MemoryEdit.withField(
                    note = note,
                    section = field.section,
                    prefix = field.prefix,
                    value = value,
                    origin = originOf()
                )
            }
        }
    }

    private fun edit(change: (String) -> String) {
        launchGuarded(onError = ::logError) {
            write(change = change)
            load()
        }
    }

    // The memory's note read, changed and written whole; made when missing, with its folder.
    private suspend fun write(change: (String) -> String) {
        val vault = vaultPath ?: return

        val before = noteContentStore.read(
            vaultPath = vault,
            path = MemoryNote.PATH
        )

        val after = change(before.orEmpty())
        if (after == before.orEmpty()) return

        if (before == null) {
            noteFileOperations.createFolder(
                vaultPath = vault,
                path = FOLDER
            )
        }

        val written = noteContentStore.write(
            vaultPath = vault,
            path = MemoryNote.PATH,
            content = after
        )

        if (written is Outcome.Failure) reduce { it.copy(hasFailed = true) }
    }

    private suspend fun forgetAll() {
        val vault = vaultPath ?: return

        val moved = noteFileOperations.move(
            vaultPath = vault,
            from = MemoryNote.PATH,
            to = RelativePath(value = "$TRASH/Memory ${originOf().substringBefore(delimiter = ',')}.md")
        )

        reduce {
            it.copy(
                isForgetting = false,
                hasFailed = moved is Outcome.Failure
            )
        }

        load()
    }

    // When and how a field was written, kept as its line's hidden comment: "2026-11-15, settings".
    private fun originOf(): String =
        "${CalendarDate.today(
            localCalendar = localCalendar,
            clock = clock
        ).iso}, settings"

    private fun logError(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
        reduce { it.copy(hasFailed = true) }
    }

    private companion object {
        const val FAILED_MESSAGE = "The chat's memory could not be read or written"
        const val TRASH = ".trash"
        val FOLDER = RelativePath(value = "Notekea")
        val PAUSE = 600.milliseconds
    }
}

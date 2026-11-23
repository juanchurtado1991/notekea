package app.notekea.settings.presentation.files

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.notes.domain.AttachmentStore
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.VaultSettingsReader
import app.notekea.core.notes.domain.VaultSettingsWriter
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Severity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

/**
 * Settings → Files (5.2) for the vault at [vaultPath], read from and written to its `.obsidian/` files ([reader],
 * [writer]) so Obsidian and Notekea agree (the developer's choice, 2026-11-11); a choice is saved a moment after it
 * is made. The folders offered are the vault's own ([attachmentStore]).
 */
class FilesSettingsViewModel(
    private val reader: VaultSettingsReader,
    private val writer: VaultSettingsWriter,
    private val attachmentStore: AttachmentStore,
    private val localCalendar: LocalCalendar,
    private val clock: Clock,
    private val vaultPath: String?
) : BaseViewModel<FilesSettingsState, FilesSettingsIntent, Nothing>(
    initialState = FilesSettingsState(hasVault = vaultPath != null)
) {

    // Each field waits for its own pause, so typing in one never drops another's write (bug hunt, 2026-11-11).
    private val saving = mutableMapOf<Field, Job>()

    init {
        vaultPath?.let { vault ->
            launchLogged(
                message = FAILED_MESSAGE,
                severity = Severity.Warn
            ) { load(vault = vault) }
        }
    }

    override fun handleIntent(intent: FilesSettingsIntent) {
        when (intent) {
            FilesSettingsIntent.OnShown -> vaultPath?.let { vault ->
                launchLogged(
                    message = FAILED_MESSAGE,
                    severity = Severity.Warn
                ) { load(vault = vault) }
            }

            is FilesSettingsIntent.OnAttachmentsChange -> change(field = Field.ATTACHMENTS) {
                it.copy(attachments = intent.folder)
            }

            is FilesSettingsIntent.OnDailyFolderChange -> change(field = Field.DAILY) {
                it.copy(dailyFolder = intent.folder)
            }

            is FilesSettingsIntent.OnDailyFormatChange -> change(field = Field.DAILY) {
                it.copy(dailyFormat = intent.format)
            }

            is FilesSettingsIntent.OnTemplatesFolderChange -> change(field = Field.TEMPLATES) {
                it.copy(templatesFolder = intent.folder)
            }
        }
    }

    // The code review of 2026-11-22: a load over a choice not saved yet put the old value back, which the save then
    // wrote. A field waiting to be saved, or changed before the load ends, keeps what the person chose.
    private suspend fun load(vault: String) {
        val jobs = saving.toMap()
        val unsaved = jobs.filterValues { it.isActive }.keys
        val attachments = reader.attachmentFolderPath(vaultPath = vault).orEmpty()
        val daily = reader.dailyNotes(vaultPath = vault)
        val templates = reader.templates(vaultPath = vault)

        // Every folder, the empty ones too: a new folder made for the attachments could not be chosen.
        val folders = attachmentStore.folders(vaultPath = vault)

        reduce { current ->
            val kept = unsaved + Field.entries.filter {
                saving[it] !== jobs[it]
            }

            val isDailyKept = Field.DAILY in kept
            val format = if (isDailyKept) current.dailyFormat else daily?.format.orEmpty()
            val dailyFolder = if (isDailyKept) current.dailyFolder else daily?.folder.orEmpty()

            val shownAttachments =
                if (Field.ATTACHMENTS in kept) current.attachments else attachments

            val templatesFolder = if (Field.TEMPLATES in kept) {
                current.templatesFolder
            } else {
                templates?.folder.orEmpty()
            }

            current.copy(
                attachments = shownAttachments,
                dailyFolder = dailyFolder,
                dailyFormat = format,
                templatesFolder = templatesFolder,
                folders = folders.map { it.value }.sorted(),
                formats = DateFormatChoice.choicesOf(
                    today = CalendarDate.today(
                        localCalendar = localCalendar,
                        clock = clock
                    ),
                    current = format
                )
            )
        }
    }

    private fun change(
        field: Field,
        transform: (FilesSettingsState) -> FilesSettingsState
    ) {
        val vault = vaultPath ?: return
        reduce { transform(it).copy(hasFailed = false) }
        saving[field]?.cancel()

        saving[field] = launchLogged(
            message = FAILED_MESSAGE,
            severity = Severity.Warn
        ) {
            delay(duration = PAUSE)

            val saved = save(
                vault = vault,
                field = field,
                state = state.value
            )

            if (saved is Outcome.Failure) reduce { it.copy(hasFailed = true) }
        }
    }

    private suspend fun save(
        vault: String,
        field: Field,
        state: FilesSettingsState
    ): Outcome<Unit, *> = when (field) {
        Field.ATTACHMENTS -> writer.setAttachmentFolder(
            vaultPath = vault,
            folder = state.attachments.trim()
        )

        Field.DAILY -> writer.setDailyNotes(
            vaultPath = vault,
            folder = state.dailyFolder.trim(),
            format = state.dailyFormat.trim()
        )

        Field.TEMPLATES -> writer.setTemplatesFolder(
            vaultPath = vault,
            folder = state.templatesFolder.trim()
        )
    }

    // The file a field goes to.
    private enum class Field { ATTACHMENTS, DAILY, TEMPLATES }

    private companion object {
        const val FAILED_MESSAGE = "Files settings failed"
        val PAUSE = 600.milliseconds
    }
}

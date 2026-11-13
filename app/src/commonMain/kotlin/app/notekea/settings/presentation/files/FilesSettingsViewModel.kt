package app.notekea.settings.presentation.files

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.notes.domain.AttachmentStore
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.VaultSettingsReader
import app.notekea.core.notes.domain.VaultSettingsWriter
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger
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
    private val logger = Logger.withTag(tag = LOG_TAG)

    // Each field waits for its own pause, so typing in one never drops another's write (bug hunt, 2026-11-11).
    private val saving = mutableMapOf<Field, Job>()

    init {
        vaultPath?.let { vault ->
            launchGuarded(onError = ::logError) { load(vault = vault) }
        }
    }

    override fun handleIntent(intent: FilesSettingsIntent) {
        when (intent) {
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

    private suspend fun load(vault: String) {
        val attachments = reader.attachmentFolderPath(vaultPath = vault).orEmpty()
        val daily = reader.dailyNotes(vaultPath = vault)
        val templates = reader.templates(vaultPath = vault)

        val files = (attachmentStore.files(vaultPath = vault) as? Outcome.Success)?.value.orEmpty()

        reduce {
            it.copy(
                attachments = attachments,
                dailyFolder = daily?.folder.orEmpty(),
                dailyFormat = daily?.format.orEmpty(),
                templatesFolder = templates?.folder.orEmpty(),
                folders = foldersOf(files = files),
                formats = DateFormatChoice.choicesOf(
                    today = localCalendar.momentOf(instant = clock.now()).date,
                    current = daily?.format.orEmpty()
                )
            )
        }
    }

    // Every folder that holds a file, and each above it, by its path; the hidden ones (.obsidian, .trash) left out.
    private fun foldersOf(files: List<RelativePath>): List<String> = files
        .flatMap { file ->
            generateSequence(seed = file.parent) { it.parent }
                .takeWhile {
                    !it.isRoot
                }
                .map { it.value }
        }
        .distinct()
        .filterNot { folder ->
            folder.split('/').any { it.startsWith(prefix = ".") }
        }
        .sorted()

    private fun change(
        field: Field,
        transform: (FilesSettingsState) -> FilesSettingsState
    ) {
        val vault = vaultPath ?: return
        reduce { transform(it).copy(hasFailed = false) }
        saving[field]?.cancel()

        saving[field] = launchGuarded(onError = ::logError) {
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

    private fun logError(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
    }

    // The file a field goes to.
    private enum class Field { ATTACHMENTS, DAILY, TEMPLATES }

    private companion object {
        const val LOG_TAG = "FilesSettingsViewModel"
        const val FAILED_MESSAGE = "Files settings failed"
        val PAUSE = 600.milliseconds
    }
}

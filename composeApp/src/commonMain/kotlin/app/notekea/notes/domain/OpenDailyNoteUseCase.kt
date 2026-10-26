package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalCalendar
import kotlin.time.Clock

/**
 * Opens the daily note of [date], today's where the user is when it is null (`ROADMAP.md` 1.10), creating it when it
 * does not exist yet: in the folder and with the name of the vault's Daily notes settings (`Journal/YYYY-MM-DD` without
 * them), its missing folders created too, starting from the settings' template filled in ([DailyNoteTemplate]), or
 * empty when there is none or it cannot be read. An existing note is opened as it is.
 */
class OpenDailyNoteUseCase(
    private val vaultSettingsReader: VaultSettingsReader,
    private val noteFileOperations: NoteFileOperations,
    private val noteContentStore: NoteContentStore,
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) {
    private val dailyNotes = DailyNotes()
    private val template = DailyNoteTemplate()

    suspend operator fun invoke(
        vaultPath: String,
        date: CalendarDate? = null
    ): Outcome<FollowedLink, NotesError> {
        val settings = vaultSettingsReader.dailyNotes(vaultPath = vaultPath) ?: DailyNoteSettings.DEFAULT
        val now = localCalendar.momentOf(instant = clock.now())
        val day = date ?: now.date
        val path = dailyNotes.pathOf(date = day, settings = settings)
        if (noteFileOperations.exists(vaultPath = vaultPath, path = path)) {
            return Outcome.Success(value = FollowedLink(path = path, isNew = false))
        }
        createFolders(vaultPath = vaultPath, folder = path.parent ?: RelativePath.ROOT)?.let { return it }
        when (val created = noteFileOperations.createFile(vaultPath = vaultPath, path = path)) {
            is Outcome.Failure -> return if (created.error is NotesError.NameTaken) {
                Outcome.Success(value = FollowedLink(path = path, isNew = false))
            } else {
                created
            }

            is Outcome.Success -> Unit
        }
        val text = settings.template
            ?.let { noteContentStore.read(vaultPath = vaultPath, path = RelativePath(value = it + MARKDOWN)) }
            ?.let {
                template.fill(
                    template = it,
                    moment = now.copy(date = day),
                    title = path.nameWithoutExtension,
                    dateFormat = settings.format
                )
            }
        if (!text.isNullOrEmpty()) {
            val written = noteContentStore.write(vaultPath = vaultPath, path = path, content = text)
            if (written is Outcome.Failure) return written
        }
        return Outcome.Success(value = FollowedLink(path = path, isNew = true))
    }

    /** Creates [folder] and the folders above it that are missing; a failure, or null when they all exist. */
    private suspend fun createFolders(
        vaultPath: String,
        folder: RelativePath
    ): Outcome.Failure<NotesError>? {
        val missing = generateSequence(seed = folder) { it.parent }
            .filterNot { it.isRoot }
            .toList()
            .reversed()
            .filterNot { noteFileOperations.exists(vaultPath = vaultPath, path = it) }
        missing.forEach { path ->
            val created = noteFileOperations.createFolder(vaultPath = vaultPath, path = path)
            if (created is Outcome.Failure && created.error !is NotesError.NameTaken) return created
        }
        return null
    }

    companion object {
        private const val MARKDOWN = RelativePath.MARKDOWN_EXTENSION
    }
}

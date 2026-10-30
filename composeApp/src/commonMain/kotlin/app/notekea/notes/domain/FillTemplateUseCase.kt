package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.LocalCalendar
import kotlin.time.Clock

/**
 * Reads the `template` note and fills it in for a note named `title` (`ROADMAP.md` 1.12): `{{date}}` and `{{time}}` are
 * now, where the user is, in the vault's Templates formats ([TemplateSettings]); the result is split into frontmatter
 * and body. A template that cannot be read fails with [NotesError.EntryMissing].
 */
class FillTemplateUseCase(
    private val vaultSettingsReader: VaultSettingsReader,
    private val noteContentStore: NoteContentStore,
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) {
    private val filler = TemplateFiller()

    suspend operator fun invoke(
        vaultPath: String,
        template: RelativePath,
        title: String
    ): Outcome<NoteDocument, NotesError> {
        val text = noteContentStore.read(vaultPath = vaultPath, path = template)
            ?: return Outcome.Failure(error = NotesError.EntryMissing(path = template.value))
        val settings = vaultSettingsReader.templates(vaultPath = vaultPath) ?: TemplateSettings.DEFAULT
        val filled = filler.fill(
            template = text,
            moment = localCalendar.momentOf(instant = clock.now()),
            title = title,
            dateFormat = settings.dateFormat,
            timeFormat = settings.timeFormat
        )
        return Outcome.Success(value = NoteDocument.fromText(text = filled))
    }
}

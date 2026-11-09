package app.notekea.templates.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TemplateSettings
import app.notekea.core.notes.domain.VaultSettingsReader
import app.notekea.core.notes.domain.createMissingFolders

/**
 * Creates the vault's templates folder ([TemplateSettings.folder]) with an example template named `exampleName` holding
 * `exampleText`; an example already there is kept. It is written whole, never created empty first: the vault's watcher
 * lists a new file at once, and an empty template would insert nothing. Returns the example's path.
 */
class CreateTemplatesFolderUseCase(
    private val vaultSettingsReader: VaultSettingsReader,
    private val noteFileOperations: NoteFileOperations,
    private val noteContentStore: NoteContentStore
) {
    suspend operator fun invoke(
        vaultPath: String,
        exampleName: String,
        exampleText: String
    ): Outcome<RelativePath, NotesError> {
        val settings =
            vaultSettingsReader.templates(vaultPath = vaultPath) ?: TemplateSettings.DEFAULT

        val folder = RelativePath(value = settings.folder)

        createMissingFolders(
            noteFileOperations = noteFileOperations,
            vaultPath = vaultPath,
            folder = folder
        )
            ?.let {
                return it
            }

        val example = folder.child(name = exampleName + RelativePath.MARKDOWN_EXTENSION)

        val hasExample = noteFileOperations.exists(
            vaultPath = vaultPath,
            path = example
        )

        if (hasExample) return Outcome.Success(value = example)

        return when (
            val written = noteContentStore.write(
                vaultPath = vaultPath,
                path = example,
                content = exampleText
            )
        ) {
            is Outcome.Failure -> written
            is Outcome.Success -> Outcome.Success(value = example)
        }
    }
}

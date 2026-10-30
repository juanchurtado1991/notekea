package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Creates the vault's templates folder ([TemplateSettings.folder], `Templates` by default) with an example template
 * named `exampleName` holding `exampleText` (`ROADMAP.md` 1.12), so someone who never used a template sees one at once.
 * An example already there is kept as it is. Returns the example's path.
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
        val settings = vaultSettingsReader.templates(vaultPath = vaultPath) ?: TemplateSettings.DEFAULT
        val folder = RelativePath(value = settings.folder)
        createMissingFolders(noteFileOperations = noteFileOperations, vaultPath = vaultPath, folder = folder)
            ?.let { return it }
        val example = folder.child(name = exampleName + RelativePath.MARKDOWN_EXTENSION)
        if (noteFileOperations.exists(vaultPath = vaultPath, path = example)) return Outcome.Success(value = example)
        when (val created = noteFileOperations.createFile(vaultPath = vaultPath, path = example)) {
            is Outcome.Failure -> return created
            is Outcome.Success -> Unit
        }
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

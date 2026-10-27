package app.notekea.notes.domain

/**
 * The picture a note's card shows in a folder's grid (`ROADMAP.md` 1.11): the first image the note embeds
 * (`![[plate.png]]` or `![alt](plate.png)`) that is a file of the vault, found as the editor finds it, or null when it
 * embeds none or its text cannot be read.
 */
class GetNoteCoverUseCase(
    private val noteContentStore: NoteContentStore,
    private val vaultSettingsReader: VaultSettingsReader
) {
    private val embedFinder = EmbedFinder()

    suspend operator fun invoke(
        vaultPath: String,
        note: RelativePath,
        files: Collection<RelativePath>
    ): RelativePath? {
        val text = noteContentStore.read(vaultPath = vaultPath, path = note) ?: return null
        val embeds = embedFinder.embeds(text = text)
        if (embeds.isEmpty()) return null
        val setting = vaultSettingsReader.attachmentFolderPath(vaultPath = vaultPath)
        val resolver = AttachmentResolver(
            files = files,
            attachmentFolder = AttachmentFolder(setting = setting).folderFor(note = note)
        )
        return embeds.firstNotNullOfOrNull { embed ->
            resolver.resolve(embed = embed, note = note)?.takeIf {
                AttachmentKind.of(name = it.name) == AttachmentKind.IMAGE
            }
        }
    }
}

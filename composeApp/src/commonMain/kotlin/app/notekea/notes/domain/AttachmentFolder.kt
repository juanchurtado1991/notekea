package app.notekea.notes.domain

/**
 * Where new attachments of a note are saved, as Obsidian's `attachmentFolderPath` setting says (`ROADMAP.md` 1.8):
 * `/` the vault root, `./` the note's own folder, `./images` a folder inside the note's folder, any other path a fixed
 * folder of the vault. Without the setting, Notekea uses `attachments/` at the vault root.
 */
class AttachmentFolder(private val setting: String?) {
    fun folderFor(
        note: RelativePath
    ): RelativePath {
        val written = setting?.trim()
        val noteFolder = note.parent ?: RelativePath.ROOT
        return when {
            written == null || written.isEmpty() -> RelativePath(value = DEFAULT_FOLDER)

            written == VAULT_ROOT -> RelativePath.ROOT

            written == NOTE_FOLDER || written == NOTE_FOLDER_SHORT -> noteFolder

            written.startsWith(prefix = NOTE_FOLDER) -> resolvedFrom(folder = noteFolder, target = written)
                ?: noteFolder

            else -> resolvedFrom(folder = RelativePath.ROOT, target = written) ?: RelativePath.ROOT
        }
    }

    companion object {
        private const val DEFAULT_FOLDER = "attachments"
        private const val NOTE_FOLDER = "./"
        private const val NOTE_FOLDER_SHORT = "."
        private const val VAULT_ROOT = "/"
    }
}

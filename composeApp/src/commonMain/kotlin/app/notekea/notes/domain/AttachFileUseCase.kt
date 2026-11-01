package app.notekea.notes.domain

import app.notekea.core.domain.LocalTimeFormatter
import app.notekea.core.domain.Outcome
import kotlin.time.Clock

/**
 * Attaches a file to `note` (`ROADMAP.md` 1.8) and gives the path to embed: a file already in the vault is linked
 * where it is; anything else is saved into the note's attachments folder ([AttachmentFolder], from Obsidian's
 * settings) under the first free name, numbered as Obsidian does. A pasted image is named `Pasted image
 * <local date and time>.png`. A saved file's name never holds what would end its embed (`[]`, `#`, `^`, `|`).
 */
class AttachFileUseCase(
    private val attachmentStore: AttachmentStore,
    private val vaultSettingsReader: VaultSettingsReader,
    private val noteFileOperations: NoteFileOperations,
    private val localTimeFormatter: LocalTimeFormatter,
    private val clock: Clock
) {
    suspend operator fun invoke(
        vaultPath: String,
        note: RelativePath,
        source: AttachmentSource
    ): Outcome<RelativePath, NotesError> {
        if (source is AttachmentSource.File) {
            attachmentStore.pathInVault(vaultPath = vaultPath, absolutePath = source.absolutePath)?.let {
                return Outcome.Success(value = it)
            }
        }
        val folder = AttachmentFolder(setting = vaultSettingsReader.attachmentFolderPath(vaultPath = vaultPath))
            .folderFor(note = note)
        val name = linkable(
            name = when (source) {
                is AttachmentSource.Content -> source.name

                is AttachmentSource.File -> source.absolutePath.substringAfterLast(delimiter = RelativePath.SEPARATOR)
                    .substringAfterLast(delimiter = WINDOWS_SEPARATOR)
            }
        )
        return createUnderFreeName(
            noteFileOperations = noteFileOperations,
            vaultPath = vaultPath,
            folder = folder,
            baseName = name.substringBeforeLast(delimiter = EXTENSION_MARK),
            extension = name.removePrefix(prefix = name.substringBeforeLast(delimiter = EXTENSION_MARK))
        ) { path ->
            when (source) {
                is AttachmentSource.Content -> attachmentStore.write(
                    vaultPath = vaultPath,
                    path = path,
                    bytes = source.bytes
                )

                is AttachmentSource.File -> attachmentStore.copy(
                    vaultPath = vaultPath,
                    absolutePath = source.absolutePath,
                    path = path
                )
            }
        }
    }

    /**
     * [name] as an embed can hold it: brackets become parentheses (`Game [v0].nsp` is `Game (v0).nsp`, where `]]`
     * ended the embed) and `#`, `^` and `|`, which start a heading, a block or a size, become spaces.
     */
    private fun linkable(
        name: String
    ): String = name.replace(oldChar = '[', newChar = '(').replace(oldChar = ']', newChar = ')')
        .replace(regex = LINK_BREAKERS, replacement = " ").replace(regex = SPACES, replacement = " ").trim()

    /** A screenshot or copied image as Obsidian names it when pasted. */
    fun pastedImage(
        bytes: ByteArray
    ): AttachmentSource = AttachmentSource.Content(
        name = PASTED_IMAGE_PREFIX + localTimeFormatter.compact(instant = clock.now()) + PNG_EXTENSION,
        bytes = bytes
    )

    companion object {
        private const val EXTENSION_MARK = "."
        private val LINK_BREAKERS = Regex(pattern = "[#^|]")
        private const val PASTED_IMAGE_PREFIX = "Pasted image "
        private const val PNG_EXTENSION = ".png"
        private val SPACES = Regex(pattern = " {2,}")
        private const val WINDOWS_SEPARATOR = "\\"
    }
}

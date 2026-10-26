package app.notekea.notes.presentation.editor

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.AttachFileUseCase
import app.notekea.notes.domain.AttachmentFolder
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.AttachmentResolver
import app.notekea.notes.domain.AttachmentSource
import app.notekea.notes.domain.AttachmentStore
import app.notekea.notes.domain.Embed
import app.notekea.notes.domain.EmbedFinder
import app.notekea.notes.domain.EmbeddedNoteText
import app.notekea.notes.domain.LinkResolver
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.notes.domain.EmbedText
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultSettingsReader
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * The files and notes embedded in the open note of the vault at [vaultPath] (`ROADMAP.md` 1.8), for the editor: it
 * finds them in the text, resolves them among the vault's files (listed once and again after a change), reads and
 * decodes the images, keeping each decoded picture until its file changes, reads the text an embedded note shows, and
 * attaches new files, giving the embed to write. A note that embeds itself, or a missing note or heading, stays a link.
 */
class EditorAttachments(
    private val vaultPath: String,
    private val attachmentStore: AttachmentStore,
    private val vaultSettingsReader: VaultSettingsReader,
    private val attachFile: AttachFileUseCase,
    private val dispatchers: AppDispatchers
) {
    private val embedFinder = EmbedFinder()
    private val embeddedNoteText = EmbeddedNoteText()
    private var files: List<RelativePath>? = null
    private var folderSetting: String? = null
    private val images = mutableMapOf<RelativePath, ImageBitmap?>()

    /** The files embedded in [text] of [note], resolved, with the images read. */
    suspend fun viewsOf(
        note: RelativePath,
        text: String
    ): List<AttachmentView> {
        val embeds = embedFinder.embeds(text = text, includeNotes = true)
        if (embeds.isEmpty()) return emptyList()
        var vaultFiles = vaultFiles()
        val resolver = { files: List<RelativePath> ->
            AttachmentResolver(
                files = files,
                attachmentFolder = AttachmentFolder(setting = folderSetting).folderFor(note = note)
            )
        }
        // A file added outside Notekea since the last listing is found by listing again once.
        if (embeds.any { !it.isNote && resolver(vaultFiles).resolve(embed = it, note = note) == null }) {
            vaultFiles =
                vaultFiles(refresh = true)
        }
        val notes = LinkResolver(notePaths = vaultFiles.filter { it.isMarkdown })
        return embeds.mapNotNull { embed ->
            if (embed.isNote) return@mapNotNull noteView(embed = embed, note = note, notes = notes)
            val path = resolver(vaultFiles).resolve(embed = embed, note = note)
            val kind = AttachmentKind.of(name = path?.name ?: embed.target)
            val image = if (path != null && kind == AttachmentKind.IMAGE) imageOf(path = path) else null
            AttachmentView(
                embed = embed,
                path = path,
                kind = kind,
                image = image,
                isUnreadable = path != null && kind == AttachmentKind.IMAGE && image == null
            )
        }
    }

    /** The note [embed] shows in [note], with the text it shows; null when it is missing or [note] itself. */
    private suspend fun noteView(
        embed: Embed,
        note: RelativePath,
        notes: LinkResolver
    ): AttachmentView? {
        val name = embed.target.substringBefore(delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR)
        if (name.isBlank()) return null
        val path = notes.closestWiki(target = name, source = note)?.takeIf { it != note } ?: return null
        val text = (attachmentStore.read(vaultPath = vaultPath, path = path) as? Outcome.Success)?.value
            ?.decodeToString() ?: return null
        val shown = embeddedNoteText.of(text = text, target = embed.target) ?: return null
        return AttachmentView(embed = embed, kind = AttachmentKind.NOTE, path = path, noteText = shown)
    }

    /**
     * Attaches [source] to [note] and gives the embed to write for it, `![[name.png]]` or with its path, with [width]
     * when given. A new image ([withDefaultWidth]) gets a width the user sees and can change: its own, at most
     * [DEFAULT_IMAGE_WIDTH], so a small picture is never stretched.
     */
    suspend fun attach(
        note: RelativePath,
        source: AttachmentSource,
        width: Int? = null,
        withDefaultWidth: Boolean = false
    ): Outcome<String, NotesError> = when (
        val attached = attachFile(
            vaultPath = vaultPath,
            note = note,
            source = source
        )
    ) {
        is Outcome.Failure -> attached

        is Outcome.Success -> {
            val shownWidth = width ?: if (withDefaultWidth) defaultWidth(path = attached.value) else null
            Outcome.Success(
                value = EmbedText(files = vaultFiles(refresh = true)).of(path = attached.value, width = shownWidth)
            )
        }
    }

    /** The width a new image is embedded at: its own, at most [DEFAULT_IMAGE_WIDTH]; null for other files. */
    private suspend fun defaultWidth(
        path: RelativePath
    ): Int? = if (AttachmentKind.of(name = path.name) == AttachmentKind.IMAGE) {
        imageOf(path = path)?.width?.coerceAtMost(maximumValue = DEFAULT_IMAGE_WIDTH)
    } else {
        null
    }

    /** A pasted image, named as Obsidian names it. */
    fun pastedImage(
        bytes: ByteArray
    ): AttachmentSource = attachFile.pastedImage(bytes = bytes)

    /** Lists the vault's files again, and forgets the pictures, after the vault changed outside Notekea. */
    fun forgetFiles() {
        files = null
        images.clear()
    }

    private suspend fun vaultFiles(
        refresh: Boolean = false
    ): List<RelativePath> {
        if (refresh || files == null) {
            folderSetting = vaultSettingsReader.attachmentFolderPath(vaultPath = vaultPath)
            files = (attachmentStore.files(vaultPath = vaultPath) as? Outcome.Success)?.value.orEmpty()
        }
        return files.orEmpty()
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun imageOf(
        path: RelativePath
    ): ImageBitmap? = images.getOrPut(key = path) {
        val bytes = (attachmentStore.read(vaultPath = vaultPath, path = path) as? Outcome.Success)?.value
        bytes?.let {
            withContext(context = dispatchers.default) {
                try {
                    it.decodeToImageBitmap()
                } catch (_: IllegalArgumentException) {
                    // Not a picture Skia can read (a broken or renamed file): it shows as a file to open instead.
                    null
                }
            }
        }
    }

    companion object {
        /** The widest a new image is embedded, in the note's units (dp), so it fits beside the text it belongs to. */
        const val DEFAULT_IMAGE_WIDTH = 400
    }
}

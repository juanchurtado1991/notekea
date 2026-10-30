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
import app.notekea.notes.domain.EmbedProblem
import app.notekea.notes.domain.LinkResolver
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.notes.domain.EmbedText
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultSettingsReader
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * The files and notes embedded in the open note of the vault at [vaultPath] (`ROADMAP.md` 1.8), for the editor: it
 * finds them in the text, resolves them among the vault's files (listed once and again after a change), reads and
 * decodes the images, keeping the pictures the note shows (and no others) until their files change, reads the text an embedded note shows, and
 * attaches new files, giving the embed to write. An embedded note shows its own embeds in turn, three notes deep at
 * most; one that is missing, lacks its section, comes back around (itself included) or is too deep says so instead.
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
    private val images = mutableMapOf<RelativePath, CachedImage>()

    // Counts the readings of a note's embeds, so a picture no reading has used since the last one is let go.
    private var reading = 0
    private var noteResolver: LinkResolver? = null
    private var noteResolverFiles: List<RelativePath>? = null

    /**
     * The files and notes embedded in [text] of [note], resolved, with the images read and each embedded note's own
     * embeds inside it, down to [EmbedProblem.MAX_EMBED_DEPTH] notes (`ROADMAP.md` 1.15).
     */
    suspend fun viewsOf(
        note: RelativePath,
        text: String
    ): List<AttachmentView> {
        val current = ++reading
        val views = viewsIn(note = note, text = text, chain = listOf(note))
        // Only the pictures this note shows stay decoded: the notes shown before keep nothing in memory. A reading
        // started meanwhile (the text changed again) prunes for itself.
        if (reading == current) images.values.removeAll { it.reading < current }
        return views
    }

    /** The embeds of [text], written in [note]; [chain] holds the notes shown around it, [note] last. */
    private suspend fun viewsIn(
        note: RelativePath,
        text: String,
        chain: List<RelativePath>
    ): List<AttachmentView> {
        val embeds = embedFinder.embeds(text = text, includeNotes = true)
        if (embeds.isEmpty()) return emptyList()
        var vaultFiles = vaultFiles()
        val resolverOf = { files: List<RelativePath> ->
            AttachmentResolver(
                files = files,
                attachmentFolder = AttachmentFolder(setting = folderSetting).folderFor(note = note)
            )
        }
        var resolver = resolverOf(vaultFiles)
        // A file added outside Notekea since the last listing is found by listing again once.
        if (embeds.any { !it.isNote && resolver.resolve(embed = it, note = note) == null }) {
            vaultFiles = vaultFiles(refresh = true)
            resolver = resolverOf(vaultFiles)
        }
        val notes = noteResolverOf(files = vaultFiles)
        return embeds.mapNotNull { embed ->
            // A note embedded inside a sentence stays a link, so only notes on their own line are read.
            if (embed.isNote) return@mapNotNull embed.takeIf { it.isOwnLine }?.let { noteView(it, notes, chain) }
            fileView(embed = embed, path = resolver.resolve(embed = embed, note = note))
        }
    }

    /** The vault's notes as links resolve them, built again only when the vault's files were listed again. */
    private fun noteResolverOf(
        files: List<RelativePath>
    ): LinkResolver = noteResolver?.takeIf { noteResolverFiles === files }
        ?: LinkResolver(notePaths = files.filter { it.isMarkdown }).also {
            noteResolver = it
            noteResolverFiles = files
        }

    /** A file [embed] found at [path] (null when missing), with its picture read when it is an image. */
    private suspend fun fileView(
        embed: Embed,
        path: RelativePath?
    ): AttachmentView {
        val kind = AttachmentKind.of(name = path?.name ?: embed.target)
        val image = if (path != null && kind == AttachmentKind.IMAGE) imageOf(path = path) else null
        return AttachmentView(
            embed = embed,
            path = path,
            kind = kind,
            image = image,
            isUnreadable = path != null && kind == AttachmentKind.IMAGE && image == null
        )
    }

    /**
     * The note [embed] shows inside the last note of [chain], with its text and that text's own embeds, or why it
     * cannot show it: missing, without the section, already in [chain] or too deep. Null for an embed of a section
     * of the same note (`![[#Heading]]`), which stays a link.
     */
    private suspend fun noteView(
        embed: Embed,
        notes: LinkResolver,
        chain: List<RelativePath>
    ): AttachmentView? {
        val name = embed.target.substringBefore(delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR)
        if (name.isBlank()) return null
        val view = AttachmentView(embed = embed, kind = AttachmentKind.NOTE, path = null)
        val path = notes.closestWiki(target = name, source = chain.last())
            ?: return view.copy(problem = EmbedProblem.MISSING_NOTE)
        val problem = when {
            path in chain -> EmbedProblem.CIRCULAR
            chain.size >= EmbedProblem.MAX_EMBED_DEPTH + 1 -> EmbedProblem.TOO_DEEP
            else -> null
        }
        if (problem != null) return view.copy(path = path, problem = problem)
        val text = (attachmentStore.read(vaultPath = vaultPath, path = path) as? Outcome.Success)?.value
            ?.decodeToString() ?: return view.copy(problem = EmbedProblem.MISSING_NOTE)
        val shown = embeddedNoteText.of(text = text, target = embed.target)
            ?: return view.copy(path = path, problem = EmbedProblem.MISSING_SECTION)
        return view.copy(
            path = path,
            noteText = shown,
            nested = viewsIn(note = path, text = shown, chain = chain + path)
        )
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

    /** The picture at [path], decoded once and kept while the notes read use it (null when it cannot be read). */
    private suspend fun imageOf(
        path: RelativePath
    ): ImageBitmap? {
        val cached = images[path] ?: CachedImage(reading = reading, image = decodeImage(path = path))
        images[path] = cached.copy(reading = reading)
        return cached.image
    }

    private suspend fun decodeImage(
        path: RelativePath
    ): ImageBitmap? {
        val bytes = (attachmentStore.read(vaultPath = vaultPath, path = path) as? Outcome.Success)?.value
        return bytes?.let {
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

    /** A decoded picture, and the reading of a note's embeds that last used it. */
    private data class CachedImage(
        val reading: Int,
        val image: ImageBitmap?
    )

    companion object {
        /** The widest a new image is embedded, in the note's units (dp), so it fits beside the text it belongs to. */
        const val DEFAULT_IMAGE_WIDTH = 400
    }
}

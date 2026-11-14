package app.notekea.editor.presentation.viewmodel

import androidx.compose.ui.text.TextRange
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.StringPatterns.NEW_LINE
import app.notekea.core.domain.StringPatterns.WINDOWS_NEW_LINE
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.YouTubeLinks
import app.notekea.editor.domain.AttachmentSource
import app.notekea.editor.domain.CompressStep
import app.notekea.editor.domain.Embed
import app.notekea.editor.domain.FilesShownHere
import app.notekea.editor.domain.FormatAction
import app.notekea.editor.domain.VideoLimits
import app.notekea.editor.presentation.attachments.AttachmentTools
import app.notekea.editor.presentation.attachments.EditorAttachments
import app.notekea.editor.presentation.model.EditorEffect
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorState
import app.notekea.editor.presentation.model.VideoCompression
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * The files of the open note: what it embeds, resolved a moment after the last change; a pasted image and dropped or
 * picked files attached and embedded where the selection was; a file opened with its app; a missing one located.
 * Reading the clipboard, the picker and copying take time: when another note opened meanwhile, the edit is dropped.
 */
internal class EditorFiles(
    private val host: EditorHost,
    private val vaultPath: String,
    private val tools: AttachmentTools,
    private val textEdits: EditorTextEdits,
    private val onPickCancelled: () -> Unit
) {
    private val attachments = EditorAttachments(
        vaultPath = vaultPath,
        attachmentStore = tools.attachmentStore,
        vaultSettingsReader = tools.vaultSettingsReader,
        attachFile = tools.attachFile,
        youTubeDetails = tools.youTubeDetails,
        dispatchers = tools.dispatchers,
        filePosters = tools.filePosters
    )
    private var attachmentsJob: Job? = null
    private var compression: Job? = null
    private val compressing = Mutex()

    fun onIntent(intent: EditorIntent.Attachment) {
        when (intent) {
            EditorIntent.OnPaste -> paste()
            is EditorIntent.OnFilesDrop -> drop(paths = intent.paths)
            EditorIntent.OnAttachmentPick -> pick()
            is EditorIntent.OnAttachmentOpen -> open(path = intent.path)
            is EditorIntent.OnAttachmentLocate -> locate(embed = intent.embed)
            EditorIntent.OnActionErrorDismiss -> host.reduce { it.copy(actionError = null) }
            EditorIntent.OnVideoCompressionCancel -> compression?.cancel()
            EditorIntent.OnCompressErrorDismiss -> host.reduce { it.copy(compressError = null) }
        }
    }

    /** Lists the vault's files again when another note opens: one may have been added outside Notekea. */
    fun forgetFiles() = attachments.forgetFiles()

    /** The files [body] embeds, resolved and read a moment after the last change, so typing is never held up. */
    fun update(body: String) {
        val note = host.state.value.notePath ?: return
        attachmentsJob?.cancel()

        attachmentsJob = host.launch {
            delay(duration = PAUSE)

            val views = attachments.viewsOf(
                note = note,
                text = body
            )

            host.reduce {
                if (it.notePath == note) it.copy(attachments = views) else it
            }

            // The pictures of its PDFs and videos come after the note shows, each made once.
            if (!attachments.makePosters(views = views)) return@launch

            val pictured = attachments.viewsOf(
                note = note,
                text = body
            )

            host.reduce {
                if (it.notePath == note) it.copy(attachments = pictured) else it
            }
        }
    }

    /** Mod+V: a copied image is attached and embedded; copied text replaces the selection, as a paste does. */
    private fun paste() {
        val note = host.state.value.notePath ?: return
        if (!host.state.value.isEditable) return

        host.launch {
            val image =
                withContext(context = tools.dispatchers.io) { tools.clipboardReader.image() }

            if (!isOpen(note = note)) return@launch

            when {
                // An image the clipboard cannot give is explained, instead of pasting nothing or other text.
                image is Outcome.Failure -> {
                    host.reduce { it.copy(actionError = image.error) }

                    return@launch
                }

                image is Outcome.Success && image.value != null -> {
                    attachAndEmbed(
                        note = note,
                        sources = listOf(attachments.pastedImage(bytes = image.value))
                    )

                    return@launch
                }
            }

            val text = withContext(context = tools.dispatchers.io) { tools.clipboardReader.text() }

            if (!isOpen(note = note)) return@launch

            // A YouTube video's address pasted alone becomes its card, then gets its title (#141).
            val youTubeId = text?.trim()?.let { YouTubeLinks.idOf(url = it) }

            if (text != null && youTubeId != null) {
                pasteYouTube(
                    note = note,
                    id = youTubeId,
                    address = text.trim()
                )

                return@launch
            }

            // The text being edited only has `\n` breaks, as [app.notekea.core.notes.domain.NoteDocument] says.
            text?.replace(
                oldValue = WINDOWS_NEW_LINE,
                newValue = NEW_LINE
            )?.let { pasted ->
                host.buffer.edit {
                    val start = selection.min

                    replace(
                        start = start,
                        end = selection.max,
                        text = pasted
                    )

                    selection = TextRange(index = start + pasted.length)
                }
            }
        }
    }

    /**
     * Writes the embed of a pasted YouTube video, `![](address)`, then asks YouTube once for its title and picture and
     * puts the title in the embed, as long as the same note is open and the embed is still there as written.
     */
    private suspend fun pasteYouTube(
        note: RelativePath,
        id: String,
        address: String
    ) {
        val embed = "![]($address)"
        textEdits.format(action = FormatAction.Embed(markdown = embed))

        val title = tools.youTubeDetails.fetch(id = id) ?: return
        if (!isOpen(note = note)) return
        val titled = "![${title.replace(BRACKETS, " ").trim()}]($address)"

        host.buffer.edit {
            val at = toString().indexOf(string = embed)

            if (at >= 0) {
                replace(
                    start = at,
                    end = at + embed.length,
                    text = titled
                )
            }
        }
    }

    private fun drop(paths: List<String>) {
        val note = host.state.value.notePath ?: return
        val hasNothingToDrop = !host.state.value.isEditable || paths.isEmpty()
        if (hasNothingToDrop) return

        host.launch {
            attachAndEmbed(
                note = note,
                sources = paths.map { AttachmentSource.File(absolutePath = it) }
            )
        }
    }

    /** Image or file: the system's picker takes the keyboard, so the selection is kept for the embed. */
    private fun pick() {
        val note = host.state.value.notePath ?: return
        if (!host.state.value.isEditable) return
        textEdits.keepSelectionForPicker()

        host.launch {
            val picked = tools.attachmentPicker.pickFile()

            if (picked == null) {
                onPickCancelled()
            } else {
                attachAndEmbed(
                    note = note,
                    sources = listOf(AttachmentSource.File(absolutePath = picked))
                )
            }
        }
    }

    /**
     * Attaches every source to [note] and writes their embeds, one per line, in place of the selection; images get a
     * width. The files stay attached if another note opened meanwhile, but no embed goes in it.
     */
    private suspend fun attachAndEmbed(
        note: RelativePath,
        sources: List<AttachmentSource>
    ) {
        val embeds = mutableListOf<String>()

        for (source in sources) {
            val small = smallEnough(source = source) ?: continue

            when (
                val attached = attachments.attach(
                    note = note,
                    source = small,
                    withDefaultWidth = true
                )
            ) {
                is Outcome.Success -> embeds += attached.value
                is Outcome.Failure -> host.reduce { it.copy(actionError = attached.error) }
            }

            // A compressed copy is let go once it is attached; the video it came from stays where it was.
            val copy = (small as? AttachmentSource.File)?.takeIf {
                small != source
            }

            copy?.let { tools.videoCompressor.discard(path = it.absolutePath) }
        }

        if (!isOpen(note = note)) {
            textEdits.releasePickerSelection(isCancelled = false)
        } else if (embeds.isNotEmpty()) {
            textEdits.format(action = FormatAction.Embed(markdown = embeds.joinToString(NEW_LINE)))
        }
    }

    /**
     * [source], or a copy of it under GitHub's 100 MB when it is a bigger video (#141), compressed with its progress
     * above the note; null when that fails or is cancelled, and the video is then not attached.
     */
    private suspend fun smallEnough(source: AttachmentSource): AttachmentSource? {
        if (source !is AttachmentSource.File) return source

        val name = source
            .absolutePath
            .substringAfterLast(delimiter = '/')
            .substringAfterLast(delimiter = '\\')

        val isVideo = AttachmentKind.of(name = name) == AttachmentKind.VIDEO

        val isOver = isVideo && tools.videoCompressor.isOverLimit(
            source = source.absolutePath,
            maxBytes = VideoLimits.MAX_BYTES
        )

        if (!isOver) return source

        // One video at a time: two at once ran two FFmpegs, and Cancel stopped only the newest.
        return compressing.withLock {
            compressed(
                source = source,
                name = name
            )
        }
    }

    private suspend fun compressed(
        source: AttachmentSource.File,
        name: String
    ): AttachmentSource? {
        var small: AttachmentSource? = null

        host.reduce {
            it.copy(
                videoCompression = VideoCompression(name = name),
                compressError = null
            )
        }

        coroutineScope {
            compression = launch {
                tools
                    .videoCompressor
                    .compress(
                        source = source.absolutePath,
                        maxBytes = VideoLimits.MAX_BYTES
                    )
                    .collect { step -> small = onCompressStep(step = step) ?: small }
            }
        }

        compression = null
        host.reduce { it.copy(videoCompression = null) }

        return small
    }

    // Shows how far a video's compression is; the compressed copy once it is done.
    private fun onCompressStep(step: CompressStep): AttachmentSource? {
        when (step) {
            is CompressStep.Downloading -> host.reduce {
                it.copy(
                    videoCompression = it.videoCompression?.copy(
                        isDownloading = true,
                        fraction = step.fraction
                    )
                )
            }

            is CompressStep.Compressing -> host.reduce {
                it.copy(
                    videoCompression = it.videoCompression?.copy(
                        isDownloading = false,
                        fraction = step.fraction
                    )
                )
            }

            is CompressStep.Failed -> host.reduce { it.copy(compressError = step.error) }

            is CompressStep.Done -> return AttachmentSource.File(absolutePath = step.path)
        }

        return null
    }

    /** Whether [note], which an edit started from, is still the one in the text. */
    private fun isOpen(note: RelativePath): Boolean = host.state.value.notePath == note

    // A document Notekea reads and a video open in a tab of their own; any other file with its app.
    private fun open(path: RelativePath) {
        val isShownHere = FilesShownHere.holds(path = path)
        if (isShownHere) return host.emit(effect = EditorEffect.OpenFile(path = path))

        host.launch {
            val opened = tools.fileOpener.open(
                vaultPath = vaultPath,
                path = path
            )

            if (opened is Outcome.Failure) {
                host.reduce { it.copy(actionError = opened.error) }
            }
        }
    }

    /** Locate…: the chosen file, copied into the vault when it is outside it, takes the missing embed's place. */
    private fun locate(embed: Embed) {
        val note = host.state.value.notePath ?: return

        val text = host
            .buffer
            .text
            .toString()

        // An embed inside an embedded note is at a place of that note's text, not of this one: past its end it
        // stopped the editor, and within it another text would be replaced (the code review of 2026-11-13).
        val isHere = embed.range.first >= 0 && embed.range.last < text.length
        if (!isHere) return
        val written = text.substring(range = embed.range)
        if (!written.startsWith(prefix = "![")) return

        host.launch {
            val picked = tools.attachmentPicker.pickFile() ?: return@launch
            val source = AttachmentSource.File(absolutePath = picked)

            when (
                val attached = attachments.attach(
                    note = note,
                    source = source,
                    width = embed.width
                )
            ) {
                is Outcome.Failure -> host.reduce { it.copy(actionError = attached.error) }

                is Outcome.Success -> if (isOpen(note = note)) {
                    replaceEmbed(
                        embed = embed,
                        written = written,
                        replacement = attached.value
                    )
                }
            }
        }
    }

    /** Writes [replacement] where [embed] was, unless the text there changed meanwhile. */
    private fun replaceEmbed(
        embed: Embed,
        written: String,
        replacement: String
    ) {
        val text = host
            .buffer
            .text
            .toString()

        val isEmbedChanged =
            embed.range.last >= text.length || text.substring(range = embed.range) != written

        if (isEmbedChanged) return

        host.buffer.edit {
            replace(
                start = embed.range.first,
                end = embed.range.last + 1,
                text = replacement
            )
        }
    }

    companion object {
        private val PAUSE = 150.milliseconds

        // A title's brackets would end its embed's alt text early.
        private val BRACKETS = Regex(pattern = "[\\[\\]]")
    }
}

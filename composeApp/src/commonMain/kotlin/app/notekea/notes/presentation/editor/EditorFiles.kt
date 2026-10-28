package app.notekea.notes.presentation.editor

import androidx.compose.ui.text.TextRange
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.StringPatterns.NEW_LINE
import app.notekea.notes.domain.AttachmentSource
import app.notekea.notes.domain.Embed
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * The files of the open note (`ROADMAP.md` 1.8, 1.15): what it embeds, resolved and read a moment after the last
 * change; a pasted image, dropped or picked files attached and embedded where the selection was; a file opened with its
 * app; and a missing one located. [onPickCancelled] gives the keyboard back to the text when the file picker closes
 * without a choice.
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
        dispatchers = tools.dispatchers
    )
    private var attachmentsJob: Job? = null

    fun onIntent(
        intent: EditorIntent
    ) {
        when (intent) {
            EditorIntent.OnPaste -> paste()
            is EditorIntent.OnFilesDrop -> drop(paths = intent.paths)
            EditorIntent.OnAttachmentPick -> pick()
            is EditorIntent.OnAttachmentOpen -> open(path = intent.path)
            is EditorIntent.OnAttachmentLocate -> locate(embed = intent.embed)
            EditorIntent.OnActionErrorDismiss -> host.reduce { it.copy(actionError = null) }
            else -> Unit
        }
    }

    /** Lists the vault's files again when another note opens: one may have been added outside Notekea. */
    fun forgetFiles() = attachments.forgetFiles()

    /** The files [body] embeds, resolved and read a moment after the last change, so typing is never held up. */
    fun update(
        body: String
    ) {
        val note = host.state.value.notePath ?: return
        attachmentsJob?.cancel()
        attachmentsJob = host.launch {
            delay(duration = PAUSE)
            val views = attachments.viewsOf(note = note, text = body)
            host.reduce { if (it.notePath == note) it.copy(attachments = views) else it }
        }
    }

    /** Mod+V: a copied image is attached and embedded; copied text replaces the selection, as a paste does. */
    private fun paste() {
        if (!host.state.value.isEditable) return
        host.launch {
            val image = withContext(context = tools.dispatchers.io) { tools.clipboardReader.image() }
            when {
                // An image the clipboard cannot give is explained, instead of pasting nothing or other text.
                image is Outcome.Failure -> {
                    host.reduce { it.copy(actionError = image.error) }
                    return@launch
                }

                image is Outcome.Success && image.value != null -> {
                    attachAndEmbed(sources = listOf(attachments.pastedImage(bytes = image.value)))
                    return@launch
                }
            }
            val text = withContext(context = tools.dispatchers.io) { tools.clipboardReader.text() }
            text?.let { pasted ->
                host.buffer.edit {
                    val start = selection.min
                    replace(start = start, end = selection.max, text = pasted)
                    selection = TextRange(index = start + pasted.length)
                }
            }
        }
    }

    private fun drop(
        paths: List<String>
    ) {
        if (!host.state.value.isEditable || paths.isEmpty()) return
        host.launch { attachAndEmbed(sources = paths.map { AttachmentSource.File(absolutePath = it) }) }
    }

    /** Image or file: the system's picker takes the keyboard, so the selection is kept for the embed. */
    private fun pick() {
        if (!host.state.value.isEditable) return
        textEdits.keepSelectionForPicker()
        host.launch {
            val picked = tools.attachmentPicker.pickFile()
            if (picked == null) {
                onPickCancelled()
            } else {
                attachAndEmbed(sources = listOf(AttachmentSource.File(absolutePath = picked)))
            }
        }
    }

    /** Attaches every source and writes their embeds, one per line, in place of the selection; images get a width. */
    private suspend fun attachAndEmbed(
        sources: List<AttachmentSource>
    ) {
        val note = host.state.value.notePath ?: return
        val embeds = mutableListOf<String>()
        for (source in sources) {
            when (val attached = attachments.attach(note = note, source = source, withDefaultWidth = true)) {
                is Outcome.Success -> embeds += attached.value
                is Outcome.Failure -> host.reduce { it.copy(actionError = attached.error) }
            }
        }
        if (embeds.isNotEmpty()) textEdits.format(action = FormatAction.Embed(markdown = embeds.joinToString(NEW_LINE)))
    }

    private fun open(
        path: RelativePath
    ) {
        host.launch {
            val opened = tools.fileOpener.open(vaultPath = vaultPath, path = path)
            if (opened is Outcome.Failure) host.reduce { it.copy(actionError = opened.error) }
        }
    }

    /** Locate…: the chosen file, copied into the vault when it is outside it, takes the missing embed's place. */
    private fun locate(
        embed: Embed
    ) {
        val note = host.state.value.notePath ?: return
        val written = host.buffer.text.toString().substring(range = embed.range)
        host.launch {
            val picked = tools.attachmentPicker.pickFile() ?: return@launch
            val source = AttachmentSource.File(absolutePath = picked)
            when (val attached = attachments.attach(note = note, source = source, width = embed.width)) {
                is Outcome.Failure -> host.reduce { it.copy(actionError = attached.error) }
                is Outcome.Success -> replaceEmbed(embed = embed, written = written, replacement = attached.value)
            }
        }
    }

    /** Writes [replacement] where [embed] was, unless the text there changed meanwhile. */
    private fun replaceEmbed(
        embed: Embed,
        written: String,
        replacement: String
    ) {
        val text = host.buffer.text.toString()
        if (embed.range.last >= text.length || text.substring(range = embed.range) != written) return
        host.buffer.edit { replace(start = embed.range.first, end = embed.range.last + 1, text = replacement) }
    }

    companion object {
        private val PAUSE = 150.milliseconds
    }
}

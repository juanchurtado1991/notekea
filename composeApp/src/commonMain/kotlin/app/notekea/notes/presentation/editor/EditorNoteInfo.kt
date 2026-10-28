package app.notekea.notes.presentation.editor

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.time.LocalMoment
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.notes.domain.OutlineFinder
import app.notekea.notes.domain.PropertyKeys
import app.notekea.notes.domain.PropertyValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/**
 * What the editor tells about the open note besides its text: its outline (`ROADMAP.md` 1.9), what it holds and when
 * its file was created and modified for Note info (`ROADMAP.md` 1.14), and today's date where the user is. The outline
 * and the counts are found a moment after the last keystroke, so typing is never held up.
 */
internal class EditorNoteInfo(
    private val host: EditorHost,
    private val vaultPath: String,
    private val noteInfoTools: NoteInfoTools,
    private val propertyTools: PropertyTools,
    private val dispatchers: AppDispatchers
) {
    private val outlineFinder = OutlineFinder()
    private var outlineJob: Job? = null
    private var statsJob: Job? = null

    /** The headings of [body], found a moment after the last change. */
    fun updateOutline(
        body: String
    ) {
        val note = host.state.value.notePath ?: return
        outlineJob?.cancel()
        outlineJob = host.launch {
            delay(duration = PAUSE)
            val outline = outlineFinder.headings(text = body)
            host.reduce { if (it.notePath == note) it.copy(outline = outline) else it }
        }
    }

    /** Counts what the note holds once typing pauses, away from the UI thread (links are parsed). */
    fun updateStats(
        body: String
    ) {
        val note = host.state.value.notePath ?: return
        statsJob?.cancel()
        statsJob = host.launch {
            delay(duration = PAUSE)
            val tags = propertyTagsOf(content = host.state.value.properties)
            val stats = withContext(context = dispatchers.default) {
                noteInfoTools.getNoteStats(body = body, propertyTags = tags)
            }
            host.reduce { if (it.notePath == note) it.copy(info = it.info.copy(stats = stats)) else it }
        }
    }

    /** Reads when the note's file was created and modified. */
    fun readTimes() {
        val note = host.state.value.notePath ?: return
        host.launch {
            val times = noteInfoTools.noteTimesReader.times(vaultPath = vaultPath, path = note)
            host.reduce {
                if (it.notePath != note) return@reduce it
                it.copy(
                    info = it.info.copy(createdAt = momentOf(times.createdAt), modifiedAt = momentOf(times.modifiedAt))
                )
            }
        }
    }

    /** Now, where the user is: the note was just saved. */
    fun now(): LocalMoment? = momentOf(instant = propertyTools.clock.now())

    fun today() = propertyTools.localCalendar.momentOf(instant = propertyTools.clock.now()).date

    private fun momentOf(
        instant: Instant?
    ): LocalMoment? = instant?.let { propertyTools.localCalendar.momentOf(instant = it) }

    /** The tags listed in the note's properties, which Note info counts with the text's. */
    private fun propertyTagsOf(
        content: FrontmatterContent
    ): List<String> = (content as? FrontmatterContent.Properties)?.properties.orEmpty()
        .filter { PropertyKeys.isTags(key = it.key) }
        .flatMap { (it.value as? PropertyValue.Items)?.items.orEmpty() }

    companion object {
        private val PAUSE = 150.milliseconds
    }
}

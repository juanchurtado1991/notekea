package app.notekea.tree.data

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.FenceTracker
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NoteTask
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.TaskFinder
import app.notekea.core.notes.domain.TaskMarker

/**
 * Collects the [tasks] of a note's body read line by line ([TaskFinder]), each with the heading above it and its date:
 * the Tasks plugin's `📅 2026-11-02`, or else a date written in its text (`ROADMAP.md` 2.11). A note's text is read once
 * for all of what the cache keeps, so this sees its lines as they pass.
 */
internal class NoteTaskCollector {
    private val finder = TaskFinder()
    private val fences = FenceTracker()
    private val outline = OutlineFinder()
    private val stripper = MarkdownLineStripper()
    private val momentFormat = MomentFormat()
    private var offset = 0
    private var heading: String? = null

    val tasks: List<NoteTask>
        field = mutableListOf<NoteTask>()

    fun read(
        line: String
    ) {
        if (!fences.read(text = line)) {
            if (line.startsWith(prefix = HEADING_MARK)) {
                outline.headings(text = line).firstOrNull()?.let { heading = it.text }
            }
            if (TASK_MARK in line) {
                finder.markerIn(line = line, lineStart = offset)?.let { tasks += taskOf(line = line, marker = it) }
            }
        }
        offset += line.length + 1
    }

    private fun taskOf(
        line: String,
        marker: TaskMarker
    ): NoteTask {
        val plain = PLUGIN_DATE.replace(input = line, replacement = StringPatterns.EMPTY)
            .replace(regex = BLOCK_ID, replacement = StringPatterns.EMPTY)
        return NoteTask(
            stateIndex = marker.stateIndex,
            endIndex = offset + line.length,
            text = stripper.strip(line = plain, ranges = emptyList()).text.trim(),
            isDone = marker.isDone,
            heading = heading,
            date = dateOf(line = line)
        )
    }

    private fun dateOf(
        line: String
    ): CalendarDate? {
        val written = PLUGIN_DATE.find(input = line)?.groupValues?.get(index = 1)
            ?: PLAIN_DATE.find(input = line)?.value
        return written?.let { momentFormat.parseDate(text = it, pattern = MomentFormat.ISO_DATE) }
    }

    private companion object {
        const val HEADING_MARK = "#"
        const val TASK_MARK = '['
        val BLOCK_ID = Regex(pattern = "\\s+\\^[\\w-]+\\s*$")
        val PLAIN_DATE = Regex(pattern = "(?<![\\d-])\\d{4}-\\d{2}-\\d{2}(?![\\d-])")
        val PLUGIN_DATE = Regex(pattern = "\\s*📅\\s*(\\d{4}-\\d{2}-\\d{2})")
    }
}

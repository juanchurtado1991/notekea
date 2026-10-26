package app.notekea.notes.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.domain.time.MomentFormat

/**
 * Daily notes as Obsidian names them (`ROADMAP.md` 1.10): the note of a date is its name in the settings' format,
 * `.md`, in the settings' folder; and a note is a daily note when its path, read back the same way, names a date.
 */
class DailyNotes {
    private val momentFormat = MomentFormat()

    fun pathOf(
        date: CalendarDate,
        settings: DailyNoteSettings
    ): RelativePath {
        val name = momentFormat.format(moment = LocalMoment(date = date), pattern = settings.format)
        val inFolder = listOf(settings.folder, name + RelativePath.MARKDOWN_EXTENSION).filter { it.isNotEmpty() }
        return RelativePath(value = inFolder.joinToString(separator = RelativePath.SEPARATOR))
    }

    /** The date [path] is the daily note of, or null when it is not a daily note. */
    fun dateOf(
        path: RelativePath,
        settings: DailyNoteSettings
    ): CalendarDate? {
        if (!path.isMarkdown) return null
        val prefix = settings.folder.takeIf { it.isNotEmpty() }?.let { it + RelativePath.SEPARATOR }.orEmpty()
        if (!path.value.startsWith(prefix = prefix)) return null
        val name = path.value.removePrefix(prefix = prefix).dropLast(n = RelativePath.MARKDOWN_EXTENSION.length)
        return momentFormat.parseDate(text = name, pattern = settings.format)
    }

    /** [path] as a daily note, with the daily notes of [notes] just before and after it; null when it is not one. */
    fun neighborsOf(
        path: RelativePath,
        notes: Collection<RelativePath>,
        settings: DailyNoteSettings
    ): DayNeighbors? {
        val date = dateOf(path = path, settings = settings) ?: return null
        val dated = notes.mapNotNull { note -> dateOf(path = note, settings = settings)?.let { it to note } }
        return DayNeighbors(
            date = date,
            previous = dated.filter { it.first < date }.maxByOrNull { it.first }?.second,
            next = dated.filter { it.first > date }.minByOrNull { it.first }?.second
        )
    }
}

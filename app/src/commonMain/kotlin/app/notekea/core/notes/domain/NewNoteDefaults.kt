package app.notekea.core.notes.domain

import app.notekea.core.domain.time.LocalCalendar
import kotlin.time.Clock

/**
 * The properties every new note starts with (the developer's request of 2026-11-14): the day it was `created`, which
 * the file's own dates lose when a vault is copied or synced, and an empty `tags` list, ready to fill. Both are
 * what Obsidian and the queries read; a note that already has properties (a template's, a clipped page's) keeps its.
 */
class NewNoteDefaults(
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) {
    /** The frontmatter of a note created now. */
    fun header(): String {
        val today = localCalendar.momentOf(instant = clock.now()).date

        val month = today
            .month
            .toString()
            .padStart(
                length = 2,
                padChar = '0'
            )

        val day = today
            .day
            .toString()
            .padStart(
                length = 2,
                padChar = '0'
            )

        return "---\n$CREATED: ${today.year}-$month-$day\n$TAGS: []\n---\n"
    }

    /** [text] as a new note's: with the default properties unless it brings its own. */
    fun on(text: String): String = if (text.startsWith(prefix = FENCE)) text else header() + text

    companion object {
        const val CREATED = "created"
        const val TAGS = "tags"
        private const val FENCE = "---"
    }
}

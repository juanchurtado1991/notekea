package app.notekea.core.notes.domain

import app.notekea.core.domain.time.MomentFormat

/**
 * Where daily notes live and how they are named, as Obsidian keeps them in `.obsidian/daily-notes.json`: the [folder]
 * (empty for the vault root), the Moment.js [format] of their names, which may hold `/` to sort them into folders, and
 * the [template] a new one starts from (a path without `.md`, or null). A vault without the file uses [DEFAULT].
 */
data class DailyNoteSettings(
    val folder: String,
    val format: String,
    val template: String?
) {
    companion object {
        const val DEFAULT_FOLDER = "Journal"
        val DEFAULT = DailyNoteSettings(
            folder = DEFAULT_FOLDER,
            format = MomentFormat.ISO_DATE,
            template = null
        )
    }
}

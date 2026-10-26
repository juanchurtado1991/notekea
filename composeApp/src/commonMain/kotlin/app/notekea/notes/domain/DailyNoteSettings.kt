package app.notekea.notes.domain

import app.notekea.core.domain.time.MomentFormat

/**
 * Where daily notes live and how they are named (`ROADMAP.md` 1.10), as Obsidian's Daily notes plugin keeps them in
 * `.obsidian/daily-notes.json`: the [folder] (empty for the vault root), the Moment.js [format] of their names, which
 * may hold `/` to sort them into folders (`YYYY/MM/YYYY-MM-DD`), and the [template] note a new one starts from, a path
 * in the vault without `.md`, or null for an empty note. A vault without the file uses [DEFAULT]: `Journal`.
 */
data class DailyNoteSettings(
    val folder: String,
    val format: String,
    val template: String?
) {
    companion object {
        const val DEFAULT_FOLDER = "Journal"
        val DEFAULT = DailyNoteSettings(folder = DEFAULT_FOLDER, format = MomentFormat.ISO_DATE, template = null)
    }
}

package app.notekea.notes.domain

import app.notekea.core.domain.time.MomentFormat

/**
 * Where templates live and how their dates and times are written (`ROADMAP.md` 1.12), as Obsidian's Templates plugin
 * keeps them in `.obsidian/templates.json`: the [folder] in the vault, and the Moment.js [dateFormat] and [timeFormat]
 * of `{{date}}` and `{{time}}`. A vault without the file uses [DEFAULT]: `Templates`, `YYYY-MM-DD` and `HH:mm`.
 */
data class TemplateSettings(
    val folder: String,
    val dateFormat: String,
    val timeFormat: String
) {
    companion object {
        const val DEFAULT_FOLDER = "Templates"
        val DEFAULT = TemplateSettings(
            folder = DEFAULT_FOLDER,
            dateFormat = MomentFormat.ISO_DATE,
            timeFormat = MomentFormat.SHORT_TIME
        )
    }
}

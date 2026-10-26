package app.notekea.notes.domain

import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.domain.time.MomentFormat

/**
 * Fills the template a new daily note starts from, as Obsidian's Daily notes plugin does: `{{date}}` is the note's
 * date in [DailyNoteSettings.format], `{{time}}` the time it was created (`HH:mm`), `{{title}}` the note's name, and
 * `{{date:FORMAT}}` or `{{time:FORMAT}}` use their own Moment.js format. Any other text, other `{{…}}` included, stays.
 */
class DailyNoteTemplate {
    private val momentFormat = MomentFormat()

    fun fill(
        template: String,
        moment: LocalMoment,
        title: String,
        dateFormat: String
    ): String = VARIABLE.replace(input = template) { match ->
        val format = match.groups[FORMAT_GROUP]?.value
        when (match.groupValues[NAME_GROUP].lowercase()) {
            TITLE -> title
            TIME -> momentFormat.format(moment = moment, pattern = format ?: MomentFormat.SHORT_TIME)
            else -> momentFormat.format(moment = moment, pattern = format ?: dateFormat)
        }
    }

    companion object {
        private const val FORMAT_GROUP = 2
        private const val NAME_GROUP = 1
        private const val TIME = "time"
        private const val TITLE = "title"
        private val VARIABLE = Regex(
            pattern = "\\{\\{\\s*(date|time|title)\\s*(?::([^}]*?))?\\s*}}",
            option = RegexOption.IGNORE_CASE
        )
    }
}

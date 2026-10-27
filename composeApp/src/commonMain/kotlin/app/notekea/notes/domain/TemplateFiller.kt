package app.notekea.notes.domain

import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.domain.time.MomentFormat

/**
 * Fills a template in, as Obsidian's Templates and Daily notes plugins do (`ROADMAP.md` 1.10, 1.12): `{{date}}` is the
 * date in `dateFormat` (a daily note's own date, or today's), `{{time}}` the time in `timeFormat`, `{{title}}` the
 * note's name, and `{{date:FORMAT}}` or `{{time:FORMAT}}` use their own Moment.js format. Any other text, other `{{…}}`
 * included, stays.
 */
class TemplateFiller {
    private val momentFormat = MomentFormat()

    fun fill(
        template: String,
        moment: LocalMoment,
        title: String,
        dateFormat: String,
        timeFormat: String = MomentFormat.SHORT_TIME
    ): String = VARIABLE.replace(input = template) { match ->
        val format = match.groups[FORMAT_GROUP]?.value
        when (match.groupValues[NAME_GROUP].lowercase()) {
            TITLE -> title
            TIME -> momentFormat.format(moment = moment, pattern = format ?: timeFormat)
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

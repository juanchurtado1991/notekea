package app.notekea.chat.eval

/**
 * How an answer is checked for what it must hold (`docs/loop-goal-plan.md`, step 1.3): without commas, Markdown
 * emphasis and runs of spaces, so "**38** días" holds "38 días", and a day written 2026-12-06 is held by
 * "6 December", "December 6" or "6 de diciembre" too. Test code: the month names are a list on purpose.
 */
internal object FairText {
    /** [text] as it is compared: lower case, with no commas, emphasis or runs of spaces. */
    fun plain(text: String): String = text
        .replace(
            oldValue = ",",
            newValue = ""
        )
        .replace(
            regex = EMPHASIS,
            replacement = ""
        )
        .replace(
            regex = SPACES,
            replacement = " "
        )
        .lowercase()

    /** Whether [answer], already [plain], holds [wanted]: as written, or a day as words. */
    fun isHeld(
        wanted: String,
        answer: String
    ): Boolean = plain(text = wanted) in answer || daysOf(wanted = wanted).any { day ->
        // "16 december" does not hold "6 december", nor "december 16" hold "december 1".
        val whole = "(?<!\\d)${Regex.escape(literal = day)}(?!\\d)"

        Regex(pattern = whole).containsMatchIn(input = answer)
    }

    // A day written 2026-12-06 as people write it: 6 december, december 6, 6 de diciembre.
    private fun daysOf(wanted: String): List<String> {
        val day = DAY.matchEntire(input = wanted.trim()) ?: return emptyList()
        val month = day.groupValues[2].toInt()
        val date = day.groupValues[3].toInt()
        val english = ENGLISH.getOrNull(index = month - 1) ?: return emptyList()
        val spanish = SPANISH[month - 1]

        return listOf("$date $english", "$english $date", "$date de $spanish")
    }

    private val EMPHASIS = Regex(pattern = "[*`]")
    private val SPACES = Regex(pattern = "\\s+")
    private val DAY = Regex(pattern = "(\\d{4})-(\\d{2})-(\\d{2})")

    private val ENGLISH = listOf(
        "january", "february", "march", "april", "may", "june",
        "july", "august", "september", "october", "november", "december"
    )

    private val SPANISH = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
}

package app.notekea.chat.domain.plan

/**
 * The figures of a text written from notes that the notes do not hold: a price, a date or a measure a model makes up
 * reads as true as the ones it copied. Checked after writing, as its last step; one-digit numbers are left out
 * (counts, list numbers), which a text may well say without the notes writing them.
 */
internal object BackedFigures {
    private val FIGURE = Regex(pattern = "\\d+(?:[.,]\\d+)?")
    private const val MIN_DIGITS = 2

    fun missingIn(
        text: String,
        sources: List<String>
    ): List<String> {
        val held = sources
            .flatMap { source ->
                FIGURE.findAll(input = source).map { normal(figure = it.value) }
            }
            .toSet()

        return FIGURE
            .findAll(input = text)
            .map { it.value }
            .filter { figure ->
                figure.count { it.isDigit() } >= MIN_DIGITS
            }
            .filter {
                normal(figure = it) !in held
            }
            .distinct()
            .toList()
    }

    // "2,60" and "2.60" and "2.6" are one figure.
    private fun normal(figure: String): String {
        val dotted = figure.replace(
            oldChar = ',',
            newChar = '.'
        )

        return if ('.' in dotted) dotted.trimEnd('0').trimEnd('.') else dotted
    }
}

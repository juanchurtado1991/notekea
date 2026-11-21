package app.notekea.chat.domain.tool.app

/**
 * The value of a figure as notes write it, in English or Spanish style: "1,234.50", "1.234,50", "€ 120", "38,5".
 * A comma or a point followed by exactly three digits groups thousands (2.500 €, 12,000); else it marks decimals.
 */
internal object FigureValues {
    private val FIGURE = Regex(pattern = "\\d[\\d.,]*")

    /** Every figure [text] writes, as values: what a line of a note holds. */
    fun allIn(text: String): List<Double> = FIGURE
        .findAll(input = text)
        .mapNotNull { valueOf(written = it.value) }
        .toList()

    /** The first figure [text] writes; null when it writes none. */
    fun firstIn(text: String): Double? = FIGURE
        .find(input = text)
        ?.let { valueOf(written = it.value) }

    fun valueOf(written: String): Double? {
        val figure = written.trimEnd('.', ',')
        val lastComma = figure.lastIndexOf(char = ',')
        val lastPoint = figure.lastIndexOf(char = '.')

        val plain = when {
            lastComma >= 0 && lastPoint >= 0 -> {
                val decimal = if (lastComma > lastPoint) ',' else '.'
                val grouping = if (decimal == ',') '.' else ','

                figure.replace(
                    oldValue = grouping.toString(),
                    newValue = ""
                ).replace(decimal, '.')
            }

            lastComma >= 0 -> separated(
                figure = figure,
                mark = ','
            )

            lastPoint >= 0 -> separated(
                figure = figure,
                mark = '.'
            )

            else -> figure
        }

        return plain.toDoubleOrNull()
    }

    // One kind of mark: groups of three after it are thousands ("12,000", "2.500", "1.234.567"); else it is the
    // decimal mark ("38,5", "2.75").
    private fun separated(
        figure: String,
        mark: Char
    ): String {
        val parts = figure.split(mark)
        val isGrouped = parts.size > 2 || parts.last().length == THOUSANDS

        val joint = if (isGrouped) "" else "."

        return parts.joinToString(separator = joint)
    }

    /** Whether [one] and [other] are the same value, as a figure and its copy are. */
    fun isSame(
        one: Double,
        other: Double
    ): Boolean = kotlin.math.abs(one - other) < EPSILON

    private const val THOUSANDS = 3
    private const val EPSILON = 1e-9
}

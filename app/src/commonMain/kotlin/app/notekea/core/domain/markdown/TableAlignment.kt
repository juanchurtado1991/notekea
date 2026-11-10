package app.notekea.core.domain.markdown

/** How a table's column lines its cells up, as its cell of the delimiter row says; [mark] is how Notekea writes it. */
enum class TableAlignment(val mark: String) {
    NONE(mark = "---"),
    LEFT(mark = ":--"),
    CENTER(mark = ":-:"),
    RIGHT(mark = "--:");

    companion object {
        private const val SIDE = ':'
        private val DELIMITER = Regex(pattern = ":?-+:?")

        /** What a delimiter row's cell says ([delimiterCell]: `---`, `:--`, `:-:`, `--:`); null if it is not one. */
        fun of(delimiterCell: String): TableAlignment? {
            val cell = delimiterCell.trim()

            if (!DELIMITER.matches(input = cell)) return null

            val isLeft = cell.startsWith(char = SIDE)
            val isRight = cell.endsWith(char = SIDE)

            return when {
                isLeft && isRight -> CENTER
                isLeft -> LEFT
                isRight -> RIGHT
                else -> NONE
            }
        }
    }
}

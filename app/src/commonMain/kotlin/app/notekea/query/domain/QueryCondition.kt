package app.notekea.query.domain

/**
 * How a [QueryFilter] tests a note, named in a query block by [written]. [IN_FOLDER] and [HAS_TAG]
 * look at the note itself, the others at a property; [IS_EMPTY] and the note's own conditions need less to be whole.
 */
enum class QueryCondition(
    val written: String,
    val needsProperty: Boolean = true,
    val needsValue: Boolean = true
) {
    IS(written = "is"),
    CONTAINS(written = "contains"),
    MORE_THAN(written = "more than"),
    LESS_THAN(written = "less than"),
    BEFORE(written = "before"),
    AFTER(written = "after"),
    IS_EMPTY(
        written = "is empty",
        needsValue = false
    ),
    IN_FOLDER(
        written = "in folder",
        needsProperty = false
    ),
    HAS_TAG(
        written = "has tag",
        needsProperty = false
    );

    companion object {
        private val SPACES = Regex(pattern = "\\s+")

        /**
         * The condition written as [text], or null. A person writes `Is empty` or `more  than` as well, so case and
         * the spaces between words do not count.
         */
        fun of(text: String): QueryCondition? {
            val words = wordsOf(text = text)

            return entries.firstOrNull {
                it.written == words
            }
        }

        /** [text] lowercase, with one space between its words: how a keyword is matched. */
        fun wordsOf(text: String): String = text
            .trim()
            .lowercase()
            .replace(
                regex = SPACES,
                replacement = " "
            )
    }
}

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
        /** The condition written as [text], or null. */
        fun of(text: String): QueryCondition? = entries.firstOrNull {
            it.written == text
        }
    }
}

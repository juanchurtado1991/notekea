package app.notekea.query.domain

/** One test a note must pass: [property] (blank for a note's folder or tags), [condition] and the [value] to test. */
data class QueryFilter(
    val condition: QueryCondition,
    val property: String = "",
    val value: String = ""
)

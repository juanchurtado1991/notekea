package app.notekea.query.domain

/** The property a query's rows are ordered by, high first when [isDescending]. */
data class QuerySort(
    val property: String,
    val isDescending: Boolean = false
)

package app.notekea.query.domain

/**
 * A list of notes chosen by their properties: the [filters] a note must all pass, the order, and
 * the [columns] to show. Until they are chosen, the table shows the properties the filters and the sort look at.
 */
data class VaultQuery(
    val filters: List<QueryFilter> = emptyList(),
    val sort: QuerySort? = null,
    val columns: List<String>? = null
) {
    /** Every property the query names, in the order it names them. */
    val properties: List<String>
        get() {
            val tested = filters.map { it.property }.filter { it.isNotEmpty() }

            return (tested + listOfNotNull(sort?.property) + columns.orEmpty()).distinct()
        }

    /** What the table shows besides each note's name. */
    val shownColumns: List<String>
        get() = columns ?: properties
}

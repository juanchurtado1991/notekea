package app.notekea.query.domain

/**
 * A list of notes chosen by their properties (`ROADMAP.md` 2.10): the [filters] a note must all pass, the order, and
 * the [columns] to show. With no columns, the table shows the properties the filters and the sort look at.
 */
data class VaultQuery(
    val filters: List<QueryFilter> = emptyList(),
    val sort: QuerySort? = null,
    val columns: List<String> = emptyList()
) {
    /** Every property the query names, in the order it names them. */
    val properties: List<String>
        get() = (filters.map { it.property }.filter { it.isNotEmpty() } + listOfNotNull(sort?.property) + columns)
            .distinct()

    /** What the table shows besides each note's name. */
    val shownColumns: List<String>
        get() = columns.ifEmpty { properties }
}

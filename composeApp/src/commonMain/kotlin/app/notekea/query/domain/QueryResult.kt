package app.notekea.query.domain

/** What a query found: its [rows] in order, their [columns], and the properties it names that no note has. */
data class QueryResult(
    val rows: List<QueryRow> = emptyList(),
    val columns: List<String> = emptyList(),
    val unknown: List<String> = emptyList()
)

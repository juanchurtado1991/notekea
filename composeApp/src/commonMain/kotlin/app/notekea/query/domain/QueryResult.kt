package app.notekea.query.domain

/** What a query found among [total] notes: its [rows] in order, their [columns], and the properties no note has. */
data class QueryResult(
    val rows: List<QueryRow> = emptyList(),
    val columns: List<String> = emptyList(),
    val unknown: List<String> = emptyList(),
    val total: Int = 0
)

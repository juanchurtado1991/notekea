package app.notekea.query.domain

/** Why a query block cannot be read. */
enum class QueryProblem {
    UNKNOWN_KEY,
    UNKNOWN_CONDITION,
    MISSING_PART
}

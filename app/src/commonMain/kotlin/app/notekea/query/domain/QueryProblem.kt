package app.notekea.query.domain

/** Why a query block cannot be read. */
enum class QueryProblem {
    UNKNOWN_KEY,
    UNKNOWN_CONDITION,
    UNKNOWN_ORDER,
    MISSING_PART
}

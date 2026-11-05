package app.notekea.query.domain

/** What a query block's YAML reads as: a [Valid] query, or the [Invalid] line (1-based) and why. */
sealed interface QueryParse {
    data class Valid(val query: VaultQuery) : QueryParse

    data class Invalid(
        val line: Int,
        val problem: QueryProblem
    ) : QueryParse
}

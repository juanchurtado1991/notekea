package app.notekea.query.domain

/** What a property holds across the vault, which says how it can be tested. */
enum class QueryValueKind(val conditions: List<QueryCondition>) {
    TEXT(conditions = listOf(QueryCondition.IS, QueryCondition.CONTAINS, QueryCondition.IS_EMPTY)),
    NUMBER(
        conditions = listOf(
            QueryCondition.IS,
            QueryCondition.MORE_THAN,
            QueryCondition.LESS_THAN,
            QueryCondition.IS_EMPTY
        )
    ),
    DATE(
        conditions = listOf(
            QueryCondition.IS,
            QueryCondition.BEFORE,
            QueryCondition.AFTER,
            QueryCondition.IS_EMPTY
        )
    ),
    CHECKBOX(conditions = listOf(QueryCondition.IS, QueryCondition.IS_EMPTY))
}

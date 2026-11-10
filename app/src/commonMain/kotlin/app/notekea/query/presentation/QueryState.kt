package app.notekea.query.presentation

import app.notekea.query.domain.QueryCatalog
import app.notekea.query.domain.QueryResult
import app.notekea.query.domain.VaultQuery

/**
 * The Query tab: the [query], what it [found][result], and what it can [choose from][catalog].
 * [notesVersion] grows with every change to the notes queries run against.
 */
data class QueryState(
    val query: VaultQuery = VaultQuery(),
    val result: QueryResult = QueryResult(),
    val catalog: QueryCatalog = QueryCatalog(),
    val isLoading: Boolean = true,
    val notesVersion: Int = 0,
    val notice: QueryNotice? = null,
    val isDescribing: Boolean = false
)

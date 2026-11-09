package app.notekea.query.domain

/** What a query can choose from in this vault: its properties, folders and tags, in order. */
data class QueryCatalog(
    val keys: List<QueryKey> = emptyList(),
    val folders: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

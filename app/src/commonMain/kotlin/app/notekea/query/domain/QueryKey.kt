package app.notekea.query.domain

/** A property the vault's notes have: its [name] and what it mostly holds. */
data class QueryKey(
    val name: String,
    val kind: QueryValueKind
)

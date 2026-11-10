package app.notekea.query.data

import com.ghost.serialization.annotations.GhostSerialization

/** A query as the model writes it (`QueryPrompt.SCHEMA`). */
@GhostSerialization
data class QueryJsonDto(
    val filters: List<QueryFilterJsonDto> = emptyList(),
    val sort: String? = null,
    val descending: Boolean = false
)

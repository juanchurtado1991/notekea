package app.notekea.query.data

import com.ghost.serialization.annotations.GhostSerialization

/** One filter of it, its condition as the query tab writes it. */
@GhostSerialization
data class QueryFilterJsonDto(
    val property: String = "",
    val condition: String = "",
    val value: String = ""
)

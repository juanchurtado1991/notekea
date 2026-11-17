package app.notekea.query.data

import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryJsonReader
import app.notekea.query.domain.QuerySort
import app.notekea.query.domain.VaultQuery
import com.ghost.serialization.Ghost
import com.ghost.serialization.exception.GhostJsonException

/** [QueryJsonReader] over Ghost; a filter with a condition the query tab does not have fails the whole query. */
class GhostQueryJsonReader : QueryJsonReader {
    override fun read(json: String): VaultQuery? {
        val dto = try {
            Ghost.deserialize<QueryJsonDto>(json)
        } catch (_: GhostJsonException) {
            return null
        }

        // Leaving the filter out would widen the result: the notes it should have kept out would match.
        val filters = dto.filters.map { filter ->
            QueryFilter(
                condition = QueryCondition.of(text = filter.condition) ?: return null,
                property = filter.property.trim(),
                value = filter.value.trim()
            )
        }

        return VaultQuery(
            filters = filters,
            sort = dto
                .sort
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let {
                    QuerySort(
                        property = it,
                        isDescending = dto.descending
                    )
                }
        )
    }
}

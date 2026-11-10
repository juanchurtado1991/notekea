package app.notekea.query.data

import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryJsonReader
import app.notekea.query.domain.QuerySort
import app.notekea.query.domain.VaultQuery
import com.ghost.serialization.Ghost

/** [QueryJsonReader] over Ghost; a filter with a condition the query tab does not have is left out. */
class GhostQueryJsonReader : QueryJsonReader {
    override fun read(json: String): VaultQuery? {
        val dto = try {
            Ghost.deserialize<QueryJsonDto>(json)
        } catch (_: RuntimeException) {
            return null
        }

        return VaultQuery(
            filters = dto.filters.mapNotNull { filter ->
                QueryCondition.of(text = filter.condition)?.let {
                    QueryFilter(
                        condition = it,
                        property = filter.property.trim(),
                        value = filter.value.trim()
                    )
                }
            },
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

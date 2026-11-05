package app.notekea.query.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.notes.domain.FrontmatterBlocks
import app.notekea.core.notes.domain.FrontmatterReader
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.domain.YamlValues

/**
 * A query as the YAML of its ```` ```query ```` block (`ROADMAP.md` 2.10), which Obsidian shows as a code block:
 *
 * ```yaml
 * filters:
 *   - property: rating
 *     condition: more than
 *     value: 3
 *   - condition: in folder
 *     value: Books
 * sort:
 *   property: rating
 *   order: descending
 * columns: [author, rating]
 * ```
 *
 * Every part may be left out. It is read with the frontmatter's own blocks, and [read] names the first line it cannot.
 */
class QueryYaml {
    private val frontmatterReader = FrontmatterReader()

    fun read(
        text: String
    ): QueryParse {
        val delimiter = FrontmatterBlocks.DELIMITER
        val header = delimiter + NL + text.trimEnd() + NL + delimiter + NL
        val blocks = FrontmatterBlocks.of(header = header)?.blocks.orEmpty()
        var query = VaultQuery()
        var line = 1
        blocks.forEach { block ->
            val blockLine = line
            line += block.lines.size
            if (block.key == null) {
                val offset = block.lines.indexOfFirst { !isIgnored(line = it) }
                if (offset >= 0) return QueryParse.Invalid(blockLine + offset, QueryProblem.UNKNOWN_KEY)
                return@forEach
            }
            query = when (block.key) {
                FILTERS -> when (val filters = filtersOf(block = block, line = blockLine)) {
                    is Read.Done -> query.copy(filters = filters.value)
                    is Read.Failed -> return filters.invalid
                }

                SORT -> when (val sort = sortOf(block = block, line = blockLine)) {
                    is Read.Done -> query.copy(sort = sort.value)
                    is Read.Failed -> return sort.invalid
                }

                COLUMNS -> query.copy(
                    columns = columnsOf(block = block) ?: return unknownKey(line = blockLine)
                )

                else -> return QueryParse.Invalid(blockLine, QueryProblem.UNKNOWN_KEY)
            }
        }
        return QueryParse.Valid(query = query)
    }

    fun write(
        query: VaultQuery
    ): String {
        val lines = mutableListOf<String>()
        if (query.filters.isNotEmpty()) lines += FILTERS + KEY_END
        query.filters.forEach { lines += filterLines(filter = it) }
        query.sort?.let { sort ->
            lines += SORT + KEY_END
            lines += NESTED + PROPERTY + KEY_VALUE + YamlValues.written(text = sort.property)
            lines += NESTED + ORDER + KEY_VALUE + if (sort.isDescending) DESCENDING else ASCENDING
        }
        if (query.columns.isNotEmpty()) {
            val written = query.columns.joinToString(separator = ITEM_SEPARATOR) { YamlValues.writtenInline(text = it) }
            lines += COLUMNS + KEY_VALUE + YamlValues.INLINE_OPEN + written + YamlValues.INLINE_CLOSE
        }
        return lines.joinToString(separator = NL)
    }

    private fun filterLines(
        filter: QueryFilter
    ): List<String> {
        val parts = listOfNotNull(
            filter.property.takeIf { filter.condition.needsProperty }?.let { PROPERTY to YamlValues.written(text = it) },
            CONDITION to filter.condition.written,
            filter.value.takeIf { filter.condition.needsValue }?.let { VALUE to YamlValues.written(text = it) }
        )
        return parts.mapIndexed { index, (key, value) ->
            (if (index == 0) ITEM_START else ITEM_NEXT) + key + KEY_VALUE + value
        }
    }

    private fun filtersOf(
        block: FrontmatterBlocks.Block,
        line: Int
    ): Read<List<QueryFilter>> {
        val filters = mutableListOf<QueryFilter>()
        var parts = mutableMapOf<String, String>()
        var partsLine = line
        block.continuation.forEachIndexed { index, text ->
            if (isIgnored(line = text)) return@forEachIndexed
            val isNewItem = text.trimStart().startsWith(prefix = YamlValues.ITEM_MARK)
            if (isNewItem && parts.isNotEmpty()) {
                val filter = filterOf(parts = parts) ?: return failure(parts = parts, line = partsLine)
                filters += filter
                parts = mutableMapOf()
            }
            if (isNewItem) partsLine = line + index + 1
            val (key, value) = partOf(text = text) ?: return Read.Failed(unknownKey(line = line + index + 1))
            if (key !in FILTER_KEYS) return Read.Failed(unknownKey(line = line + index + 1))
            parts[key] = value
        }
        if (parts.isNotEmpty()) {
            val filter = filterOf(parts = parts) ?: return failure(parts = parts, line = partsLine)
            filters += filter
        }
        return Read.Done(value = filters)
    }

    /** The filter of [parts], or null when one is missing or its condition is unknown. */
    private fun filterOf(
        parts: Map<String, String>
    ): QueryFilter? {
        val condition = parts[CONDITION]?.let { QueryCondition.of(text = it) } ?: return null
        val property = parts[PROPERTY].orEmpty()
        val value = parts[VALUE].orEmpty()
        val isWhole = (!condition.needsProperty || property.isNotBlank()) && (!condition.needsValue || value.isNotBlank())
        return QueryFilter(condition = condition, property = property, value = value).takeIf { isWhole }
    }

    private fun failure(
        parts: Map<String, String>,
        line: Int
    ): Read.Failed {
        val isUnknown = parts[CONDITION]?.let { QueryCondition.of(text = it) == null } == true
        val problem = if (isUnknown) QueryProblem.UNKNOWN_CONDITION else QueryProblem.MISSING_PART
        return Read.Failed(invalid = QueryParse.Invalid(line = line, problem = problem))
    }

    private fun sortOf(
        block: FrontmatterBlocks.Block,
        line: Int
    ): Read<QuerySort?> {
        val parts = mutableMapOf<String, String>()
        block.continuation.forEachIndexed { index, text ->
            if (isIgnored(line = text)) return@forEachIndexed
            val part = partOf(text = text)?.takeIf { it.first in SORT_KEYS }
                ?: return Read.Failed(unknownKey(line = line + index + 1))
            parts[part.first] = part.second
        }
        val property = parts[PROPERTY]
            ?: return Read.Failed(QueryParse.Invalid(line = line, problem = QueryProblem.MISSING_PART))
        return Read.Done(value = QuerySort(property = property, isDescending = parts[ORDER] == DESCENDING))
    }

    /** The columns of [block], or null when it is no list. */
    private fun columnsOf(
        block: FrontmatterBlocks.Block
    ): List<String>? = when (val value = frontmatterReader.valueOf(block = block)) {
        is PropertyValue.Items -> value.items
        is PropertyValue.Text -> listOf(value.text)
        else -> null
    }

    /** The key and the value of a line under a key, without its list mark, indent and quotes. */
    private fun partOf(
        text: String
    ): Pair<String, String>? {
        val line = text.trim().removePrefix(prefix = YamlValues.ITEM_MARK).trim()
        if (FrontmatterBlocks.KEY_SEPARATOR !in line) return null
        val key = line.substringBefore(delimiter = FrontmatterBlocks.KEY_SEPARATOR).trim()
        val value = line.substringAfter(delimiter = FrontmatterBlocks.KEY_SEPARATOR).trim()
        return key to YamlValues.unquoted(text = value)
    }

    private fun isIgnored(
        line: String
    ): Boolean = line.isBlank() || line.trimStart().startsWith(prefix = YamlValues.COMMENT_MARK)

    private fun unknownKey(
        line: Int
    ) = QueryParse.Invalid(line = line, problem = QueryProblem.UNKNOWN_KEY)

    /** A part of the block: read, or the reason it cannot be. */
    private sealed interface Read<out T> {
        data class Done<T>(val value: T) : Read<T>

        data class Failed(val invalid: QueryParse.Invalid) : Read<Nothing>
    }

    private companion object {
        const val ASCENDING = "ascending"
        const val COLUMNS = "columns"
        const val CONDITION = "condition"
        const val DESCENDING = "descending"
        const val FILTERS = "filters"
        const val ITEM_NEXT = "    "
        const val ITEM_SEPARATOR = ", "
        const val ITEM_START = "  - "
        const val KEY_END = ":"
        const val KEY_VALUE = ": "
        const val NESTED = "  "
        const val NL = StringPatterns.NEW_LINE
        const val ORDER = "order"
        const val PROPERTY = "property"
        const val SORT = "sort"
        const val VALUE = "value"
        val FILTER_KEYS = setOf(CONDITION, PROPERTY, VALUE)
        val SORT_KEYS = setOf(ORDER, PROPERTY)
    }
}

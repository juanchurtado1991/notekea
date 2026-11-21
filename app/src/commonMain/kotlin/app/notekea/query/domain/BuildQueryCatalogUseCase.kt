package app.notekea.query.domain

import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.frontmatter.PropertyValue

/**
 * The [QueryCatalog] of the vault's notes: a property's kind is the one most of its notes give it. Names that differ
 * only in case (`Status`, `status`) are one property, under the spelling most notes use, as queries match them.
 */
class BuildQueryCatalogUseCase {
    private val names = NaturalNameComparator()

    operator fun invoke(notes: Collection<NoteEntry>): QueryCatalog {
        val spellings = notes
            .flatMap { it.metadata.properties }
            .groupBy { it.key.lowercase() }

        val keys = spellings.values.map { found ->
            QueryKey(
                name = mostOf(found.map { it.key }),
                kind = mostOf(found.map { kindOf(value = it.value) })
            )
        }

        return QueryCatalog(
            keys = keys.sortedWith(compareBy(names) { it.name }),
            folders = notes
                .flatMapTo(destination = HashSet()) { it.folders }
                .sortedWith(names),
            tags = notes
                .flatMapTo(destination = HashSet()) { it.allTags }
                .sortedWith(names)
        )
    }

    private fun <T> mostOf(found: List<T>): T = found
        .groupingBy { it }
        .eachCount()
        .maxBy { it.value }
        .key

    private fun kindOf(value: PropertyValue): QueryValueKind = when (value) {
        is PropertyValue.Number -> QueryValueKind.NUMBER
        is PropertyValue.Date -> QueryValueKind.DATE
        is PropertyValue.Checkbox -> QueryValueKind.CHECKBOX
        else -> QueryValueKind.TEXT
    }
}

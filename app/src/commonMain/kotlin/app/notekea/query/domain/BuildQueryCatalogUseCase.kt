package app.notekea.query.domain

import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.frontmatter.PropertyValue

/** The [QueryCatalog] of the vault's notes: a property's kind is the one most of its notes give it. */
class BuildQueryCatalogUseCase {
    private val names = NaturalNameComparator()

    operator fun invoke(notes: Collection<NoteEntry>): QueryCatalog {
        val kinds = notes.flatMap { it.metadata.properties }.groupBy({ it.key }, {
            kindOf(value = it.value)
        })

        val keys = kinds.map { (name, found) ->
            val kind = found
                .groupingBy { it }
                .eachCount()
                .maxBy { it.value }
                .key

            QueryKey(
                name = name,
                kind = kind
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

    private fun kindOf(value: PropertyValue): QueryValueKind = when (value) {
        is PropertyValue.Number -> QueryValueKind.NUMBER
        is PropertyValue.Date -> QueryValueKind.DATE
        is PropertyValue.Checkbox -> QueryValueKind.CHECKBOX
        else -> QueryValueKind.TEXT
    }
}

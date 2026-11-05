package app.notekea.query.domain

import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.domain.RelativePath

/** The [QueryCatalog] of the vault's notes: a property's kind is the one most of its notes give it. */
class BuildQueryCatalogUseCase {
    private val names = NaturalNameComparator()

    operator fun invoke(
        notes: Collection<NoteEntry>
    ): QueryCatalog {
        val kinds = notes.flatMap { it.metadata.properties }.groupBy({ it.key }, { kindOf(value = it.value) })
        val keys = kinds.map { (name, found) ->
            val kind = found.groupingBy { it }.eachCount().maxBy { it.value }.key
            QueryKey(name = name, kind = kind)
        }
        return QueryCatalog(
            keys = keys.sortedWith(compareBy(names) { it.name }),
            folders = notes.flatMapTo(destination = HashSet()) { foldersOf(path = it.file.path) }.sortedWith(names),
            tags = notes.flatMapTo(destination = HashSet()) { it.metadata.tags + it.metadata.textTags }
                .sortedWith(names)
        )
    }

    private fun kindOf(
        value: PropertyValue
    ): QueryValueKind = when (value) {
        is PropertyValue.Number -> QueryValueKind.NUMBER
        is PropertyValue.Date -> QueryValueKind.DATE
        is PropertyValue.Checkbox -> QueryValueKind.CHECKBOX
        else -> QueryValueKind.TEXT
    }

    private fun foldersOf(
        path: RelativePath
    ): List<String> = generateSequence(path.parent) { it.parent }.map { it.value }.toList()
}

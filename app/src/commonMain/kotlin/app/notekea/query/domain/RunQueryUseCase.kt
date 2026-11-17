package app.notekea.query.domain

import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue

// Property names ignore case too: `Status` and `status` are one property, as the catalog shows them.
/**
 * Runs a query over the vault's [NoteEntry]s, exactly, from what the note cache holds: no model is
 * involved. A note without a property passes only "is empty". Rows are in the order of the sort, notes without its
 * property last, and by name otherwise. Text matches ignore case, as a person reads them.
 */
class RunQueryUseCase {
    private val names = NaturalNameComparator()
    private val values = QueryValues()

    operator fun invoke(
        query: VaultQuery,
        notes: Collection<NoteEntry>
    ): QueryResult {
        val found = notes.filter { note ->
            query.filters.all {
                passes(
                    note = note,
                    filter = it
                )
            }
        }

        val columns = query.shownColumns

        val rows = sorted(
            notes = found,
            sort = query.sort
        ).map { note ->
            QueryRow(
                path = note.file.path,
                cells = columns.map {
                    propertyOf(
                        note = note,
                        name = it
                    )
                }
            )
        }

        val known = notes
            .flatMapTo(destination = HashSet()) { note ->
                note
                    .metadata
                    .properties
                    .map { it.key.lowercase() }
            }

        return QueryResult(
            rows,
            columns,
            unknown = query.properties.filter {
                it.lowercase() !in known
            },
            total = notes.size
        )
    }

    private fun passes(
        note: NoteEntry,
        filter: QueryFilter
    ): Boolean {
        val value = propertyOf(
            note = note,
            name = filter.property
        )?.value

        return when (filter.condition) {
            QueryCondition.IN_FOLDER -> isInFolder(
                note = note,
                folder = filter.value
            )

            QueryCondition.HAS_TAG -> note.hasTag(tag = filter.value)

            QueryCondition.IS_EMPTY -> values.isEmpty(value = value)

            // "books with rating" reaches here as "rating is" and no value: the property is there, whatever it holds.
            QueryCondition.IS -> if (filter.value.isBlank()) {
                !values.isEmpty(value = value)
            } else {
                value != null && isEqual(value = value, other = filter.value)
            }

            QueryCondition.CONTAINS ->
                value != null &&
                    contains(value = value, other = filter.value)

            QueryCondition.MORE_THAN, QueryCondition.AFTER -> compare(filter, {
                it > 0
            }, value)

            QueryCondition.LESS_THAN, QueryCondition.BEFORE -> compare(filter, {
                it < 0
            }, value)
        }
    }

    /** The note's property named [name], whatever its case: a person writes `Status` for `status`. */
    private fun propertyOf(
        note: NoteEntry,
        name: String
    ): NoteProperty? = note
        .metadata
        .properties
        .firstOrNull {
            it.key.equals(
                other = name,
                ignoreCase = true
            )
        }

    // A hand-written "books" means the folder Books, but only at a folder's end: "Book" is not "Books/".
    private fun isInFolder(
        note: NoteEntry,
        folder: String
    ): Boolean = note
        .file
        .path
        .value
        .startsWith(
            prefix = folder.trim().trim(FOLDER_MARK) + FOLDER_MARK,
            ignoreCase = true
        )

    private fun isEqual(
        value: PropertyValue,
        other: String
    ): Boolean = values.compare(value = value, other = other) == 0 ||
        values.textsOf(value = value).any {
            it.equals(
                other = other.trim(),
                ignoreCase = true
            )
        }

    private fun contains(
        value: PropertyValue,
        other: String
    ): Boolean = values.textsOf(value = value).any {
        it.contains(
            other = other.trim(),
            ignoreCase = true
        )
    }

    private fun compare(
        filter: QueryFilter,
        isPassed: (Int) -> Boolean,
        value: PropertyValue?
    ): Boolean = value?.let {
        values.compare(
            value = it,
            other = filter.value
        )
    }?.let(isPassed) == true

    private fun sorted(
        notes: List<NoteEntry>,
        sort: QuerySort?
    ): List<NoteEntry> {
        val byName = compareBy<NoteEntry, String>(names) { it.file.path.nameWithoutExtension }
        if (sort == null) return notes.sortedWith(comparator = byName)

        val keyed = notes.map { note ->
            note to propertyOf(
                note = note,
                name = sort.property
            )?.value
        }

        val (shown, missing) = keyed.partition {
            !values.isEmpty(value = it.second)
        }

        val byValue = Comparator<Pair<NoteEntry, PropertyValue?>> { a, b ->
            compareValues(
                a = requireNotNull(a.second),
                b = requireNotNull(b.second)
            )
        }

        val ordered = if (sort.isDescending) byValue.reversed() else byValue

        return shown.sortedWith(ordered.thenBy(comparator = byName) { it.first }).map { it.first } +
            missing.map { it.first }.sortedWith(comparator = byName)
    }

    /** Numbers first and dates next, each in their own order, then everything else by name: the order is total. */
    private fun compareValues(
        a: PropertyValue,
        b: PropertyValue
    ): Int {
        val rank = rankOf(value = a).compareTo(other = rankOf(value = b))
        if (rank != 0) return rank
        val written = writtenOf(value = b)

        return values.compare(
            value = a,
            other = written
        ) ?: names.compare(writtenOf(value = a), written)
    }

    private fun rankOf(value: PropertyValue): Int = when {
        value is PropertyValue.Number && value.text.toDoubleOrNull() != null -> 0

        // A date with a time is text, yet it sorts among the dates by its day.
        values.isDate(value = value) -> 1

        else -> 2
    }

    private fun writtenOf(value: PropertyValue): String = values.textsOf(
        value = value
    ).joinToString(separator = LIST_SEPARATOR)

    private companion object {
        const val FOLDER_MARK = '/'
        const val LIST_SEPARATOR = ", "
    }
}

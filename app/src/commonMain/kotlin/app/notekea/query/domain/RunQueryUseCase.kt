package app.notekea.query.domain

import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.PropertyValue

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
            val properties = note
                .metadata
                .properties
                .associateBy { it.key }

            QueryRow(
                path = note.file.path,
                cells = columns.map {
                    properties[it]
                }
            )
        }

        val known = notes
            .flatMapTo(destination = HashSet()) { note ->
                note
                    .metadata
                    .properties
                    .map { it.key }
            }

        return QueryResult(
            rows,
            columns,
            unknown = query.properties.filter {
                it !in known
            },
            total = notes.size
        )
    }

    private fun passes(
        note: NoteEntry,
        filter: QueryFilter
    ): Boolean {
        val value = note
            .metadata
            .properties
            .firstOrNull {
                it.key == filter.property
            }
            ?.value

        return when (filter.condition) {
            QueryCondition.IN_FOLDER -> isInFolder(
                note = note,
                folder = filter.value
            )

            QueryCondition.HAS_TAG -> note.hasTag(tag = filter.value)

            QueryCondition.IS_EMPTY -> values.isEmpty(value = value)

            QueryCondition.IS -> value != null && isEqual(value = value, other = filter.value)

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

    private fun isInFolder(
        note: NoteEntry,
        folder: String
    ): Boolean = note
        .file
        .path
        .value
        .startsWith(prefix = folder.trim().trim(FOLDER_MARK) + FOLDER_MARK)

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
            note to note
                .metadata
                .properties
                .firstOrNull {
                    it.key == sort.property
                }
                ?.value
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
        value is PropertyValue.Date -> 1
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

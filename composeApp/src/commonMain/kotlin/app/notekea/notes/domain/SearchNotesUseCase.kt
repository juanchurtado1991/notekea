package app.notekea.notes.domain

import app.notekea.core.domain.firstSorted

/**
 * The notes the quick switcher (`ROADMAP.md` 1.6) lists for `query`, best first: each note matches by its name, an
 * alias or its frontmatter title ([FuzzyMatcher]), whichever matches best, the name winning a tie; a search with a
 * `/` also matches the path (`rec/are` finds "Recipes/Arepas"). Equal scores put shorter names first, then names in
 * natural order. An empty search lists the most recently modified notes, as a starting point.
 */
class SearchNotesUseCase(
    private val fuzzyMatcher: FuzzyMatcher,
    private val naturalNameComparator: NaturalNameComparator
) {
    operator fun invoke(
        notes: Collection<NoteEntry>,
        query: String,
        limit: Int = DEFAULT_LIMIT
    ): List<NoteSearchResult> {
        if (query.isBlank()) return recent(notes = notes, limit = limit)
        val byName = compareBy(naturalNameComparator) { result: ScoredResult -> result.result.note.file.path.name }
        return notes
            .mapNotNull { bestMatch(note = it, query = query) }
            .firstSorted(
                n = limit,
                comparator = compareByDescending<ScoredResult> { it.score }
                    .thenBy { it.result.note.file.path.nameWithoutExtension.length }
                    .then(byName)
                    .thenBy { it.result.note.file.path.value }
            )
            .map { it.result }
    }

    private fun bestMatch(
        note: NoteEntry,
        query: String
    ): ScoredResult? {
        val name = note.file.path.nameWithoutExtension
        val aliases = (note.metadata.aliases + listOfNotNull(note.metadata.title)).filter { it != name }
        val candidates = buildList {
            add(Candidate(field = NoteMatchField.NAME, text = name, bonus = NAME_BONUS))
            aliases.forEach { add(Candidate(field = NoteMatchField.ALIAS, text = it, bonus = 0)) }
            if (RelativePath.SEPARATOR in query) {
                val path = LinkResolver.withoutMarkdownExtension(target = note.file.path.value)
                add(Candidate(field = NoteMatchField.PATH, text = path, bonus = 0))
            }
        }
        return candidates.mapNotNull { candidate ->
            fuzzyMatcher.match(query = query, text = candidate.text)?.let { match ->
                ScoredResult(
                    result = NoteSearchResult(
                        note = note,
                        field = candidate.field,
                        text = candidate.text,
                        ranges = match.ranges
                    ),
                    score = match.score + candidate.bonus
                )
            }
        }.maxByOrNull { it.score }
    }

    private fun recent(
        notes: Collection<NoteEntry>,
        limit: Int
    ): List<NoteSearchResult> = notes
        .firstSorted(
            n = limit,
            comparator = compareByDescending<NoteEntry, Long?>(nullsFirst()) {
                it.file.modifiedAt?.toEpochMilliseconds()
            }
                .then(compareBy(naturalNameComparator) { it.file.path.name })
        )
        .map { note ->
            NoteSearchResult(
                note = note,
                field = NoteMatchField.RECENT,
                text = note.file.path.nameWithoutExtension,
                ranges = emptyList()
            )
        }

    /** One text of a note to match, and the points it adds so a name beats an alias that matches as well. */
    private class Candidate(
        val field: NoteMatchField,
        val text: String,
        val bonus: Int
    )

    private class ScoredResult(
        val result: NoteSearchResult,
        val score: Int
    )

    companion object {
        /** The palette shows up to 10 results (`ROADMAP.md` 1.6). */
        const val DEFAULT_LIMIT = 10
        private const val NAME_BONUS = 4
    }
}

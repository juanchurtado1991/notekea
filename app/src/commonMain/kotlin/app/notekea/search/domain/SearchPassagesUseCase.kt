package app.notekea.search.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.FenceTracker
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.index.IndexRunError
import app.notekea.core.notes.domain.index.PassageSnippet
import app.notekea.core.notes.domain.index.SearchHit
import app.notekea.core.notes.domain.index.VectorIndex

/**
 * Hybrid search: the query reaches the index by meaning and keywords together, and each note counts once, with its best
 * passage. A chunk stays only if it holds every searched word or is close enough in meaning ([MIN_CLOSENESS]). A tag in
 * the query limits the search to the notes that have it; a query of tags alone finds nothing here.
 */
class SearchPassagesUseCase(
    private val textEmbedder: TextEmbedder,
    private val vectorIndex: VectorIndex,
    private val noteTextCache: NoteTextCache,
    private val textFolder: TextFolder,
    private val markdownLineStripper: MarkdownLineStripper,
    private val searchNoteContent: SearchNoteContentUseCase
) {
    suspend operator fun invoke(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        query: String
    ): Outcome<List<PassageResult>, IndexRunError> {
        val (tags, words) = query
            .trim()
            .split(WHITESPACE)
            .filter { it.isNotEmpty() }
            .partition {
                it.length > 1 && it.startsWith(prefix = TAG_MARK)
            }

        if (words.isEmpty()) return Outcome.Success(value = emptyList())
        val text = words.joinToString(separator = WORD_SEPARATOR)

        val vector = (textEmbedder.embed(texts = listOf(text)) as? Outcome.Success)?.value?.single()
            ?: return Outcome.Failure(error = IndexRunError.Model)

        val allowed = allowedNotes(
            vaultPath = vaultPath,
            notes = notes,
            tags = tags
        )

        val only = allowed.keys.takeIf { tags.isNotEmpty() }

        val hits = (
            vectorIndex.search(
                vaultPath,
                text,
                vector,
                CANDIDATES,
                only
            ) as? Outcome.Success
            )?.value
            ?: return Outcome.Failure(error = IndexRunError.Index)

        val texts = noteTextCache.textsOf(
            vaultPath = vaultPath,
            notes = allowed.values
        )

        val folded = words.map { textFolder.fold(text = it) }

        val results = hits
            .asSequence()
            .filter {
                it.hasWords || it.closeness >= MIN_CLOSENESS
            }
            .mapNotNull { hit ->
                val note = allowed[hit.note]

                val body = texts[hit.note]?.body?.takeIf {
                    hit.range.last < it.length
                }

                if (note == null || body == null) {
                    null
                } else {
                    resultOf(
                        note = note,
                        hit = hit,
                        body = body,
                        words = folded
                    )
                }
            }
            .distinctBy { it.note.file.path }
            .take(n = SearchNoteContentUseCase.DEFAULT_LIMIT)
            .toList()

        return Outcome.Success(value = results)
    }

    /** The notes that have every one of [tags], by path; all of them with no tag in the query. */
    private suspend fun allowedNotes(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        tags: List<String>
    ): Map<RelativePath, NoteEntry> {
        val tagged = if (tags.isEmpty()) {
            notes
        } else {
            searchNoteContent(
                vaultPath = vaultPath,
                notes = notes,
                query = tags.joinToString(separator = WORD_SEPARATOR),
                limit = notes.size
            ).map { it.note }
        }

        return tagged.associateBy { it.file.path }
    }

    /** [hit] as a result: of its chunk, the line that holds most of [words], with the heading above it. */
    private fun resultOf(
        note: NoteEntry,
        hit: SearchHit,
        body: String,
        words: List<String>
    ): PassageResult? {
        val passage = body.substring(range = hit.range)

        val lines = LINE
            .findAll(input = passage)
            .map { it.range }
            .toList()
            .ifEmpty {
                return null
            }

        val headings = headingsIn(
            body = body,
            start = hit.range.first,
            lines = lines
        )

        val best = lines
            .filterNot {
                it in headings
            }
            .maxByOrNull { range ->
                sharedWords(
                    line = passage.substring(range = range),
                    words = words
                )
            }
            ?: lines.first()

        val heading = lines
            .takeWhile {
                it.first < best.first
            }
            .lastOrNull {
                it in headings
            }
            ?.let { passage.substring(range = it) }
            ?.trimStart(HEADING_MARK)
            ?.trim()
            ?: hit.headingPath.lastOrNull()

        val offset = hit.range.first

        val (snippet, ranges) = snippetOf(
            line = passage.substring(range = best),
            words = words
        )

        return PassageResult(
            note = note,
            snippet = snippet,
            ranges = ranges,
            range = offset + best.first..offset + best.last,
            heading = heading?.takeUnless {
                it == note.file.path.nameWithoutExtension
            }
        )
    }

    /**
     * Which of [lines], ranges of the passage that starts at [start] in [body], are headings: a `# comment` in a code
     * block is none (bug hunt S8). The body before the passage is read for the fences open where it starts.
     */
    private fun headingsIn(
        body: String,
        start: Int,
        lines: List<IntRange>
    ): Set<IntRange> {
        val fences = FenceTracker()
        var lineStart = 0

        while (lineStart < start) {
            val lineEnd = body
                .indexOf(
                    char = StringPatterns.NEW_LINE_CHAR,
                    startIndex = lineStart
                )
                .takeIf {
                    it in 0 until start
                } ?: start

            fences.read(
                text = body,
                start = lineStart,
                end = lineEnd
            )

            lineStart = lineEnd + 1
        }

        val headings = mutableSetOf<IntRange>()

        lines.forEach { range ->
            val line = start + range.first..start + range.last

            val isCode = fences.read(
                text = body,
                start = line.first,
                end = line.last + 1
            )

            val isHeading = !isCode && HEADING.matches(input = body.substring(range = line))
            if (isHeading) headings += range
        }

        return headings
    }

    private fun sharedWords(
        line: String,
        words: List<String>
    ): Int = textFolder.fold(text = line).let { folded ->
        words.count {
            foundPart(
                folded = folded,
                word = it
            ) != null
        }
    }

    /**
     * What of [word] the [folded] text holds: the word, or else its longest stem (the word without up to
     * [MAX_ENDING] last letters, at least [MIN_STEM] long), so "planting" finds "plants" and is marked in the row.
     */
    private fun foundPart(
        folded: String,
        word: String
    ): String? {
        if (folded.contains(other = word)) return word

        val shortest = maxOf(MIN_STEM, word.length - MAX_ENDING)

        return (word.length - 1 downTo shortest)
            .map { word.take(n = it) }
            .firstOrNull { folded.contains(other = it) }
    }

    /** [line] as plain text, from its sentence with most of [words], as long as a row shows, with those words found. */
    private fun snippetOf(
        line: String,
        words: List<String>
    ): Pair<String, List<IntRange>> {
        val plain = markdownLineStripper.strip(
            line = line,
            ranges = emptyList()
        ).text

        // The sentence is chosen as the line was, by the words it holds in part too: a short word, a prefix or
        // another form of a word found no sentence, and the row showed none of them (bug hunt S3).
        val found = PassageSnippet.of(
            passage = plain,
            words = words.toSet(),
            normalized = textFolder::fold,
            scores = {
                sharedWords(
                    line = it,
                    words = words
                )
            }
        )

        val cut = found
            .text
            .drop(n = found.highlight.first)
            .trimStart()

        val snippet = if (cut.length > MAX_SNIPPET) {
            cut.take(n = MAX_SNIPPET).trimEnd() + ELLIPSIS
        } else {
            cut
        }

        val folded = textFolder.fold(text = snippet)

        val parts = words.mapNotNull {
            foundPart(
                folded = folded,
                word = it
            )
        }

        val ranges = parts.flatMap { word ->
            generateSequence(seed = folded.indexOf(string = word)) {
                folded.indexOf(
                    string = word,
                    startIndex = it + word.length
                )
            }.takeWhile {
                it >= 0
            }.map {
                it until it + word.length
            }
        }

        return snippet to ranges.sortedBy { it.first }
    }

    private companion object {
        // Enough chunks for many to come from one note and still fill the list.
        const val CANDIDATES = 200

        // Closeness runs from 0 to 1. Measured with the bundled model on the Sample Vault: related passages reach 0.83,
        // unrelated ones stay at 0.80.
        const val MIN_CLOSENESS = 0.82f
        const val MAX_SNIPPET = 140

        // A stem drops at most a word's ending ("-ing", "-es", "-ción") and keeps enough letters to mean something.
        const val MAX_ENDING = 3
        const val MIN_STEM = 4
        const val ELLIPSIS = "…"
        const val HEADING_MARK = '#'
        const val TAG_MARK = "#"
        const val WORD_SEPARATOR = " "
        val HEADING = Regex(pattern = "^#{1,6}\\s.*$")
        val LINE = Regex(pattern = ".+")
        val WHITESPACE = Regex(pattern = "\\s+")
    }
}

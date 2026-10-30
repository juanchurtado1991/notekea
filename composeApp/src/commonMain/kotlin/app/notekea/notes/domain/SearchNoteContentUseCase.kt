package app.notekea.notes.domain

import app.notekea.core.domain.firstSorted
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.domain.AppDispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Content search (`ROADMAP.md` 1.6, `Mod+Shift+F`; plain text until hybrid search in 2.6). Every word of `query` must
 * appear in the note's body, ignoring letter case and accents; a `#tag` in the query must be a tag of the note, in its
 * frontmatter or in its text outside code blocks, and also finds nested tags (`#recipe` finds `#recipe/venezuelan`).
 * Notes where the search appears most come first, then by name. The matching line reads as plain text, without
 * Markdown syntax. Texts come from [NoteTextCache], so only changed notes
 * are read again; matching runs on the default dispatcher and stops as soon as the caller is cancelled (the next
 * keystroke). An empty query finds nothing.
 */
class SearchNoteContentUseCase(
    private val noteTextCache: NoteTextCache,
    private val textFolder: TextFolder,
    private val naturalNameComparator: NaturalNameComparator,
    private val markdownLineStripper: MarkdownLineStripper,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        query: String,
        limit: Int = DEFAULT_LIMIT
    ): List<ContentSearchResult> {
        val terms = termsOf(query = query) ?: return emptyList()
        val texts = noteTextCache.textsOf(vaultPath = vaultPath, notes = notes)
        return withContext(context = dispatchers.default) {
            notes.mapNotNull { note ->
                ensureActive()
                texts[note.file.path]?.let { matchOf(note = note, text = it, terms = terms) }
            }.firstSorted(
                n = limit,
                comparator = compareByDescending<ContentSearchResult> { it.matchCount }
                    .then(compareBy(naturalNameComparator) { it.note.file.path.name })
                    .thenBy { it.note.file.path.value }
            )
        }
    }

    private fun termsOf(
        query: String
    ): Terms? {
        val (tags, words) = query.trim().split(WHITESPACE).filter { it.isNotEmpty() }
            .partition { it.length > 1 && it.startsWith(prefix = TAG_MARK) }
        if (tags.isEmpty() && words.isEmpty()) return null
        return Terms(
            words = words.map { textFolder.fold(text = it) },
            tags = tags.map { textFolder.fold(text = it.removePrefix(prefix = TAG_MARK)) }
        )
    }

    private fun matchOf(
        note: NoteEntry,
        text: NoteText,
        terms: Terms
    ): ContentSearchResult? {
        val wordHits = terms.words.map { word -> occurrences(text = text.folded, word = word) }
        if (wordHits.any { it.isEmpty() }) return null
        val inlineTags = if (terms.tags.isEmpty()) emptyList() else inlineTagsOf(body = text.body)
        val frontmatterTags = note.metadata.tags.map { textFolder.fold(text = it) }
        val tagHits = terms.tags.map { tag -> inlineTags.filter { isTag(name = it.name, tag = tag) }.map { it.range } }
        val hasEveryTag = terms.tags.indices.all { i ->
            tagHits[i].isNotEmpty() || frontmatterTags.any { isTag(name = it, tag = terms.tags[i]) }
        }
        if (!hasEveryTag) return null
        val hits = (wordHits.flatten() + tagHits.flatten()).sortedBy { it.first }
        val snippet = hits.firstOrNull()?.let { snippetAround(body = text.body, hits = hits, first = it.first) }
        return ContentSearchResult(
            note = note,
            ranges = snippet?.second.orEmpty(),
            matchCount = hits.size + tagHits.count { it.isEmpty() },
            snippet = snippet?.first
        )
    }

    private fun occurrences(
        text: String,
        word: String
    ): List<IntRange> = generateSequence(seed = text.indexOf(string = word)) { previous ->
        text.indexOf(string = word, startIndex = previous + word.length).takeIf { it >= 0 }
    }.takeWhile { it >= 0 }.map { it until it + word.length }.toList()

    /** `#tags` written in the body, with their folded names, skipping fenced code blocks. */
    private fun inlineTagsOf(
        body: String
    ): List<InlineTag> {
        val tags = mutableListOf<InlineTag>()
        var fence: Char? = null
        var lineStart = 0
        body.split(NEW_LINE_CHAR).forEach { line ->
            val marker = FENCE.find(input = line)?.groupValues?.get(index = 1)?.first()
            when {
                fence != null -> if (marker == fence) fence = null

                marker != null -> fence = marker

                else -> TAG.findAll(input = line).forEach { match ->
                    val range = match.range.first + lineStart..match.range.last + lineStart
                    tags += InlineTag(name = textFolder.fold(text = match.groupValues[1]), range = range)
                }
            }
            lineStart += line.length + 1
        }
        return tags
    }

    private fun isTag(
        name: String,
        tag: String
    ): Boolean = name == tag || name.startsWith(prefix = tag + NESTED_TAG_SEPARATOR)

    /**
     * The line of [body] that holds [first] as plain text ([MarkdownLineStripper]), shortened around the first match
     * when long, and [hits] moved into that snippet.
     */
    private fun snippetAround(
        body: String,
        hits: List<IntRange>,
        first: Int
    ): Pair<String, List<IntRange>> {
        val lineStart = body.lastIndexOf(char = NEW_LINE_CHAR, startIndex = first - 1) + 1
        val lineEnd = body.indexOf(char = NEW_LINE_CHAR, startIndex = first).takeIf { it >= 0 } ?: body.length
        val plain = markdownLineStripper.strip(
            line = body.substring(startIndex = lineStart, endIndex = lineEnd),
            ranges = hits.filter {
                it.first >= lineStart && it.last < lineEnd
            }.map { it.first - lineStart..it.last - lineStart }
        )
        val firstInLine = plain.ranges.firstOrNull()?.first ?: 0
        val start = maxOf(0, firstInLine - CONTEXT_BEFORE)
        val end = minOf(plain.text.length, start + MAX_SNIPPET_LENGTH)
        val prefix = if (start > 0) ELLIPSIS else ""
        val suffix = if (end < plain.text.length) ELLIPSIS else ""
        val shift = start - prefix.length
        val ranges = plain.ranges.filter {
            it.first >= start && it.last < end
        }.map { it.first - shift..it.last - shift }
        return prefix + plain.text.substring(startIndex = start, endIndex = end) + suffix to ranges
    }

    /** The search split into plain [words] and [tags] (without `#`), both folded. */
    private class Terms(
        val words: List<String>,
        val tags: List<String>
    )

    private class InlineTag(
        val name: String,
        val range: IntRange
    )

    companion object {
        /** The palette shows up to 10 results (`ROADMAP.md` 1.6). */
        const val DEFAULT_LIMIT = 10
        private const val CONTEXT_BEFORE = 40
        private const val ELLIPSIS = "…"
        private const val MAX_SNIPPET_LENGTH = 160
        private const val NESTED_TAG_SEPARATOR = "/"
        private const val TAG_MARK = "#"
        private val FENCE = Regex(pattern = "^\\s{0,3}(`{3,}|~{3,})")
        private val TAG = Regex(pattern = "(?<![\\w#/&])#([\\p{L}\\p{N}_/-]*[\\p{L}_/-][\\p{L}\\p{N}_/-]*)")
        private val WHITESPACE = Regex(pattern = "\\s+")
    }
}

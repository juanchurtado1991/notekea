package app.notekea.chat.domain

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * The properties of the notes a question names, as passages to answer from beside the index's: the index reads a
 * note's text, not its frontmatter, so "who owns the Notekea launch?" or "which books did I rate 5?" found nothing
 * (the chat probe of 2026-11-11). A note is named by a word of its name, its folder or its properties.
 */
internal object PropertyPassages {
    private const val MAX_PASSAGES = 8
    private const val MIN_WORD = 3
    private const val STEM = 4
    private val NOT_A_LETTER = Regex(pattern = "[^\\p{L}\\p{N}]+")

    private val COMMON = setOf(
        "the", "and", "what", "which", "who", "whom", "did", "does", "how", "many", "much", "are", "was", "for", "with",
        "about", "have", "has", "that", "this", "from", "into", "when", "where", "why", "your", "you", "notes", "note",
        "que", "qué", "los", "las", "del", "con", "por", "para", "cuál", "cual", "cuáles", "cuántos", "cuántas",
        "cuanto", "quién", "quien", "cuándo", "dónde", "mis", "una", "uno", "este", "esta", "nota", "notas", "tengo"
    )

    fun of(
        question: String,
        notes: Collection<NoteEntry>,
        only: Set<RelativePath>?
    ): List<AnswerSource> {
        val asked = wordsOf(text = question).filter {
            it !in COMMON
        }

        if (asked.isEmpty()) return emptyList()

        return notes
            .filter {
                only == null || it.file.path in only
            }
            .mapNotNull { note ->
                val properties = NoteProperties.of(note = note)
                if (properties.isEmpty()) return@mapNotNull null

                val named = wordsOf(
                    text = note
                        .file
                        .path
                        .value
                        .removeSuffix(suffix = ".md") + " " + properties.joinToString(" ")
                )

                val score = asked.count { word ->
                    named.any {
                        it.take(n = STEM) == word.take(n = STEM)
                    }
                }

                (note to properties).takeIf {
                    score > 0
                }?.let {
                    it to score
                }
            }
            .sortedByDescending { it.second }
            .take(n = MAX_PASSAGES)
            .map { (named, _) ->
                val (note, properties) = named

                val text =
                    "${note.file.path.nameWithoutExtension} — ${properties.joinToString(separator = "; ")}"

                AnswerSource(
                    number = 0,
                    note = note.file.path,
                    headingPath = emptyList(),
                    range = 0 until 0,
                    text = text
                )
            }
    }

    private fun wordsOf(text: String): List<String> = text
        .lowercase()
        .split(NOT_A_LETTER)
        .filter {
            it.length >= MIN_WORD
        }
}

package app.notekea.chat.presentation.model

import app.notekea.chat.domain.TurnOutcome

/**
 * What to ask next after an answer (the developer's request of 2026-11-13): the headings of the notes it read, the
 * ones it did not cite first, since those are what the answer left unsaid. At most [MOST].
 */
object FollowUps {
    const val MOST = 3

    // A clipped note's own sections, which say nothing of what is under them.
    private val PLAIN =
        setOf("original text", "videos", "pages", "notes", "summary", "resumen", "notas")
    private val CITED = Regex(pattern = """\[(\d{1,3})]""")

    fun of(entry: ChatEntry): List<String> {
        if (entry.outcome != TurnOutcome.ANSWERED) return emptyList()

        val cited = CITED
            .findAll(input = entry.answer)
            .map { it.groupValues[1].toInt() }
            .toSet()

        return entry
            .sources
            .sortedBy {
                it.number in cited
            }
            .mapNotNull {
                it
                    .headingPath
                    .lastOrNull()
                    ?.trim()
            }
            .filter { heading ->
                val isPlain = heading.lowercase() in PLAIN

                heading.isNotEmpty() &&
                    !isPlain &&
                    !heading.equals(other = entry.question, ignoreCase = true)
            }
            .distinctBy { it.lowercase() }
            .take(n = MOST)
    }
}

package app.notekea.cards.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.RelativePath

/**
 * The cards of a note as Obsidian's Spaced Repetition plugin writes them, so both apps read and schedule the same
 * cards: a note with the tag `#flashcards`, a card per `Question::Answer` line outside code, its schedule in
 * `<!--SR:!YYYY-MM-DD,interval,ease-->` at the end of the line or on the line below.
 */
object CardFormat {
    const val TAG = "#flashcards"

    // The tag in the text in any case ("#Flashcards"), or among the note's properties ("tags: [flashcards]",
    // "- flashcards"): a deck tagged there had no cards (the code review of 2026-11-13).
    private val DECK = Regex(
        pattern = "#flashcards\\b|^\\s*(tags:.*|-\\s*)\\bflashcards\\b",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
    )
    private const val SEPARATOR = "::"
    private const val FENCE = "```"
    private val SCHEDULE = Regex(pattern = "<!--SR:!(\\d{4})-(\\d{2})-(\\d{2}),(\\d+),(\\d+)-->")

    /** The cards of the note at [note] with [text]; none in a note without the deck's tag ([TAG]). */
    fun cardsOf(
        note: RelativePath,
        text: String
    ): List<StudyCard> {
        if (!DECK.containsMatchIn(input = text)) return emptyList()

        val lines = text.lines()
        var inCode = false

        return lines.mapIndexedNotNull { index, line ->
            if (line.trimStart().startsWith(prefix = FENCE)) inCode = !inCode
            if (inCode || SEPARATOR !in line) return@mapIndexedNotNull null

            cardOf(
                note = note,
                index = index,
                line = line,
                below = lines.getOrNull(index = index + 1)
            )
        }
    }

    /** [text] with [card]'s schedule written on its line, in place of the one it had. */
    fun scheduled(
        text: String,
        card: StudyCard
    ): String {
        val lines = text.lines().toMutableList()
        val due = card.due ?: return text
        val comment = "<!--SR:!${isoOf(date = due)},${card.interval},${card.ease}-->"
        val below = lines.getOrNull(index = card.line + 1)

        if (below != null && below.trim().let { SCHEDULE.matches(input = it) }) {
            lines[card.line + 1] = comment
        } else {
            lines[card.line] = SCHEDULE.replace(
                input = lines[card.line],
                replacement = ""
            ).trimEnd() + " " + comment
        }

        return lines.joinToString(separator = "\n")
    }

    /** New cards to add at a note's end, under a heading and the tag the plugin looks for. */
    fun blockOf(cards: List<Pair<String, String>>): String = buildString {
        append("\n\n## Flashcards\n\n").append(TAG).append("\n\n")

        cards.forEach { (question, answer) ->
            append(clean(question))
                .append(SEPARATOR)
                .append(clean(answer))
                .append('\n')
        }
    }

    private fun cardOf(
        note: RelativePath,
        index: Int,
        line: String,
        below: String?
    ): StudyCard? {
        val schedule = SCHEDULE.find(input = line) ?: below?.trim()?.let {
            SCHEDULE.matchEntire(input = it)
        }

        val content = SCHEDULE.replace(
            input = line,
            replacement = ""
        ).trim()

        val question = content
            .substringBefore(delimiter = SEPARATOR)
            .trim()
            .removePrefix(prefix = "- ")
            .trim()

        val answer = content.substringAfter(delimiter = SEPARATOR).trim()
        val isHalf = question.isEmpty() || answer.isEmpty()
        if (isHalf) return null

        val values = schedule
            ?.groupValues
            ?.drop(n = 1)
            ?.map { it.toInt() }

        return StudyCard(
            note = note,
            line = index,
            question = question,
            answer = answer,
            due = values?.let {
                CalendarDate(
                    year = it[0],
                    month = it[1],
                    day = it[2]
                )
            },
            interval = values?.get(3) ?: 0,
            ease = values?.get(4) ?: CardSchedule.START_EASE
        )
    }

    private fun clean(text: String): String = text
        .replace(SEPARATOR, ":")
        .replace('\n', ' ')
        .trim()

    private fun isoOf(date: CalendarDate): String =
        "${date.year}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}"
}

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

    // The sentence a card comes from, before its schedule: on its own line it would cost the card its schedule in
    // the plugin, which skips the line after a comment (its parser, read 2026-11-26).
    private const val SOURCE_OPEN = "<!--source: "
    private const val SOURCE_CLOSE = "-->"
    private val SOURCE = Regex(pattern = "<!--source:(.*?)-->")
    private const val MIN_SENTENCE = 12

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
                below = lines.getOrNull(index = index + 1),
                text = text
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
        val comment = "<!--SR:!${due.iso},${card.interval},${card.ease}-->"
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

    /**
     * New cards to add at a note's end, under a heading and the tag the plugin looks for, each with the note's
     * sentence it comes from in a comment before its schedule's, where the plugin keeps it and shows nothing.
     */
    fun blockOf(cards: List<ProposedCard>): String = buildString {
        append("\n\n## Flashcards\n\n").append(TAG).append("\n\n")

        cards.forEach {
            append(clean(it.question))
                .append(SEPARATOR)
                .append(clean(it.answer))
                .append(" ")
                .append(SOURCE_OPEN)
                .append(
                    clean(it.sentence).replace(
                        oldValue = "--",
                        newValue = "-"
                    )
                )
                .append(SOURCE_CLOSE)
                .append('\n')
        }
    }

    /** Whether [sentence] is written in [text], case, spaces and marks aside, the cards' own comments left out. */
    fun isQuoted(
        sentence: String,
        text: String
    ): Boolean = plain(text = sentence).let {
        it.length >= MIN_SENTENCE && it in plain(text = SOURCE.replace(input = text, replacement = ""))
    }

    private fun cardOf(
        note: RelativePath,
        index: Int,
        line: String,
        text: String,
        below: String?
    ): StudyCard? {
        val schedule = SCHEDULE.find(input = line) ?: below?.trim()?.let {
            SCHEDULE.matchEntire(input = it)
        }

        val source = SOURCE
            .find(input = line)
            ?.groupValues
            ?.get(1)
            ?.trim()

        val content = SOURCE.replace(
            input = SCHEDULE.replace(
                input = line,
                replacement = ""
            ),
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
            ease = values?.get(4) ?: CardSchedule.START_EASE,
            source = source,
            isOutdated = source != null && !isQuoted(sentence = source, text = text)
        )
    }

    private fun plain(text: String): String = text.lowercase().filter { it.isLetterOrDigit() }

    private fun clean(text: String): String = text
        .replace(SEPARATOR, ":")
        .replace('\n', ' ')
        .trim()
}

package app.notekea.chat.eval

import java.text.Normalizer

/**
 * The strict grade of the model tournament (`docs/spikes/2026-11-08-model-tournament.md`): an answer is right when it
 * holds every fact, is written in the question's language and is not an excerpt pasted back; a trap is right when it
 * says the notes do not answer. Facts are found without letter case or accents.
 */
object AnswerGrade {
    private const val PASTED_WORDS = 25
    private const val PASTED_STEP = 3
    private val SPANISH = setOf(
        "el", "la", "los", "las", "de", "que", "en", "se", "es", "un", "una", "por", "para", "con", "del", "al",
        "y", "no", "lo", "su"
    )
    private val ENGLISH = setOf(
        "the", "is", "are", "of", "to", "and", "in", "it", "for", "with", "on", "as", "be", "by", "this", "that",
        "was"
    )
    private val WORD = Regex(pattern = "[a-záéíóúñü]+")
    private val MARKDOWN = Regex(pattern = """(^|\s)#{1,3} [A-Z]|\|---|- \[[ x]]""")
    private val ACCENTS = Regex(pattern = "\\p{Mn}+")

    /** Whether [answer] (null when it said the notes do not answer) is right for [question], read from [excerpts]. */
    fun isRight(
        question: AnswerQuestion,
        excerpts: List<String>,
        answer: String?
    ): Boolean = when {
        question.isTrap -> answer == null

        answer == null -> false

        else -> hasFacts(question = question, text = answer) &&
            isInLanguage(question = question, answer = answer) &&
            !isPasted(answer = answer, excerpts = excerpts)
    }

    fun hasFacts(
        question: AnswerQuestion,
        text: String
    ): Boolean {
        val folded = fold(text = text)

        return question.facts.all { ways ->
            ways.any {
                fold(text = it) in folded
            }
        }
    }

    fun isInLanguage(
        question: AnswerQuestion,
        answer: String
    ): Boolean {
        val words = WORD
            .findAll(input = answer.lowercase())
            .map { it.value }
            .toList()

        val spanish = words.count {
            it in SPANISH
        }

        val english = words.count {
            it in ENGLISH
        }

        return if (question.isSpanish) spanish > english else english >= spanish
    }

    /** Markdown of a note (a heading, a table, a task box) or a run of 25 words of an excerpt, copied back. */
    fun isPasted(
        answer: String,
        excerpts: List<String>
    ): Boolean {
        if (MARKDOWN.containsMatchIn(input = answer)) return true

        val words = answer.split(Regex(pattern = "\\s+")).filter { it.isNotEmpty() }
        if (words.size < PASTED_WORDS) return false
        val read = excerpts.joinToString(separator = " ")

        return (0..words.size - PASTED_WORDS step PASTED_STEP).any { start ->
            words.subList(start, start + PASTED_WORDS).joinToString(separator = " ") in read
        }
    }

    fun fold(text: String): String = ACCENTS.replace(
        input = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD),
        replacement = ""
    )
}

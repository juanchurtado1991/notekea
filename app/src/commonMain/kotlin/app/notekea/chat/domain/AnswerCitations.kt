package app.notekea.chat.domain

/**
 * The citations of an answer written over numbered excerpts: a citation of an excerpt that was not given is dropped,
 * and the cited ones are numbered again from 1 in the order they first appear, so the answer lists only its sources.
 */
object AnswerCitations {
    private val CITATION = Regex(pattern = """\s*\[(\d+)]""")
    private val SPACE_BEFORE_MARK = Regex(pattern = """\s+([.,;:!?])""")

    /** [text], written over [read] (excerpt `[n]` is `read[n - 1]`), with its citations checked and renumbered. */
    fun of(
        text: String,
        read: List<AnswerSource>
    ): Answer {
        // Sums written out are done again: a small model adds badly.
        val checked = SumsChecked.corrected(text = text)

        val order = mutableListOf<Int>()

        CITATION.findAll(input = checked).forEach { match ->
            val excerpt = match.groupValues[1].toInt()
            val isGiven = excerpt in 1..read.size
            if (isGiven && excerpt !in order) order += excerpt
        }

        val cited = CITATION
            .replace(input = checked) { match ->
                val position = order.indexOf(element = match.groupValues[1].toInt())

                if (position < 0) "" else " [${position + 1}]"
            }

        return Answer(
            text = SPACE_BEFORE_MARK.replace(
                input = cited.trim(),
                replacement = "$1"
            ),
            sources = order.mapIndexed { index, excerpt ->
                read[excerpt - 1].copy(number = index + 1)
            },
            read = read
        )
    }
}

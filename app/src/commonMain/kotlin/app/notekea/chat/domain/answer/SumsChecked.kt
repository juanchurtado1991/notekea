package app.notekea.chat.domain.answer

/**
 * The sums an answer writes out, done again: a small model adds badly even when it shows the numbers ("9250 (4500 +
 * 300 + 2000 + 250)" for 7050, the chat probe of 2026-11-11). "a + b = r" and "r (a + b)" get the right r.
 */
internal object SumsChecked {
    private const val NUMBER = "\\d+(?:[.,]\\d+)?"
    private const val TERMS = "$NUMBER(?:\\s*[+\\-]\\s*$NUMBER)+"

    // Only additions before a result in brackets: "3 (2026-10-21)" is a date, not a sum.
    private const val ADDED = "$NUMBER(?:\\s*\\+\\s*$NUMBER)+"

    private val RESULT_AFTER = Regex(pattern = "($TERMS)(\\s*=\\s*)($NUMBER)")
    private val RESULT_BEFORE = Regex(pattern = "($NUMBER)(\\s*\\(\\s*)($ADDED)(\\s*\\))")

    fun corrected(text: String): String {
        val after = RESULT_AFTER
            .replace(input = text) { match ->
                val (terms, equals, _) = match.destructured

                "$terms$equals${written(total = totalOf(terms = terms))}"
            }

        return RESULT_BEFORE
            .replace(input = after) { match ->
                val (_, open, terms, close) = match.destructured

                "${written(total = totalOf(terms = terms))}$open$terms$close"
            }
    }

    private fun totalOf(terms: String): Double {
        val parts = Regex(pattern = "([+\\-]?)\\s*($NUMBER)").findAll(input = terms)

        return parts.sumOf { part ->
            val value = part
                .groupValues[2]
                .replace(
                    oldChar = ',',
                    newChar = '.'
                )
                .toDouble()

            if (part.groupValues[1] == "-") -value else value
        }
    }

    private fun written(total: Double): String {
        val whole = total.toLong()

        return if (total == whole.toDouble()) whole.toString() else total.toString()
    }
}

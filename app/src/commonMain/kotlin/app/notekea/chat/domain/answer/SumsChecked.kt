package app.notekea.chat.domain.answer

import kotlin.math.abs
import kotlin.math.round

/**
 * The sums an answer writes out, done again: a small model adds badly even when it shows the numbers ("9250 (4500 +
 * 300 + 2000 + 250)" for 7050, the chat probe of 2026-11-11). "a + b = r" and "r (a + b)" get the right r; a sum that
 * was right is left as it was written, with its thousands marks ("4,500 + 300 = 4,800").
 */
internal object SumsChecked {
    // "4500", "4,500", "1.250.000", "0.25": digits with marks between them, read by [valueOf].
    private const val NUMBER = "\\d+(?:[.,]\\d+)*"
    private const val TERMS = "$NUMBER(?:\\s*[+\\-]\\s*$NUMBER)+"

    // Only additions before a result in brackets: "3 (2026-10-21)" is a date, not a sum.
    private const val ADDED = "$NUMBER(?:\\s*\\+\\s*$NUMBER)+"

    private val RESULT_AFTER = Regex(pattern = "($TERMS)(\\s*=\\s*)($NUMBER)")
    private val RESULT_BEFORE = Regex(pattern = "($NUMBER)(\\s*\\(\\s*)($ADDED)(\\s*\\))")
    private val TERM = Regex(pattern = "([+\\-]?)\\s*($NUMBER)")

    // Groups of three after a mark are thousands ("4,500", "1.250.000"), in English or Spanish.
    private val THOUSANDS = Regex(pattern = "^\\d{1,3}(?:[.,]\\d{3})+$")
    private const val CLOSE = 0.000_001
    private const val DECIMALS = 1_000_000.0

    fun corrected(text: String): String {
        val after = RESULT_AFTER
            .replace(input = text) { match ->
                val (terms, equals, result) = match.destructured

                "$terms$equals${rightResult(terms = terms, written = result)}"
            }

        return RESULT_BEFORE
            .replace(input = after) { match ->
                val (result, open, terms, close) = match.destructured

                "${rightResult(terms = terms, written = result)}$open$terms$close"
            }
    }

    // The result as it was written when it is right, or when a number cannot be read; else the sum done again.
    private fun rightResult(
        terms: String,
        written: String
    ): String {
        val total = totalOf(terms = terms) ?: return written
        val given = valueOf(number = written) ?: return written

        return if (abs(x = total - given) < CLOSE) written else writtenOf(total = total)
    }

    private fun totalOf(terms: String): Double? {
        val values = TERM
            .findAll(input = terms)
            .map { part ->
                val value = valueOf(number = part.groupValues[2])

                if (part.groupValues[1] == "-") value?.unaryMinus() else value
            }
            .toList()

        return if (values.any { it == null }) null else values.filterNotNull().sum()
    }

    private fun valueOf(number: String): Double? {
        val plain = if (THOUSANDS.matches(input = number)) {
            number.filter { it.isDigit() }
        } else {
            number.replace(
                oldChar = ',',
                newChar = '.'
            )
        }

        return plain.toDoubleOrNull()
    }

    // 304 for a whole total, 0.3 for 0.1 + 0.2: never the double's own tail of digits.
    private fun writtenOf(total: Double): String {
        val rounded = round(x = total * DECIMALS) / DECIMALS
        val whole = rounded.toLong()

        return if (rounded == whole.toDouble()) whole.toString() else rounded.toString()
    }
}

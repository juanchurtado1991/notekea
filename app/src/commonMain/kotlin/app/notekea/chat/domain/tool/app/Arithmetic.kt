package app.notekea.chat.domain.tool.app

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

/**
 * Arithmetic done by the app, not asked of the model, which gets sums wrong: numbers with `+ - * / ^`, parentheses
 * and percents ("15% of 240"), read as written with the usual precedence.
 */
internal object Arithmetic {
    private val PERCENT_OF = Regex(pattern = "(\\d+(?:\\.\\d+)?)%\\s*of\\s*")
    private val PERCENT = Regex(pattern = "(\\d+(?:\\.\\d+)?)%")
    private val THOUSANDS = Regex(pattern = "(?<=\\d),(?=\\d{3}(\\D|$))")
    private val TIMES = Regex(pattern = "(?<=[\\d)])\\s*[x×·]\\s*(?=[\\d(])")
    private val TOKEN = Regex(pattern = "\\d+(?:\\.\\d+)?|[-+*/^()]")
    private const val NEGATE = "~"
    private const val DECIMALS = 10_000L
    private const val DECIMAL_PLACES = 4
    private const val WHOLE_LIMIT = 1e15

    // A minus before a number binds less than a power: -2^2 is -(2^2), as it is written by hand.
    private val PRECEDENCE = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2, NEGATE to 3, "^" to 4)
    private val RIGHT = setOf("^", NEGATE)

    /** What [expression] comes to; null when it is not arithmetic or divides by zero. */
    fun valueOf(expression: String): Double? {
        val written = expression
            .lowercase()
            .replace(
                regex = THOUSANDS,
                replacement = ""
            )
            .replace(
                regex = PERCENT_OF,
                replacement = "($1/100)*"
            )
            .replace(
                regex = PERCENT,
                replacement = "($1/100)"
            )
            .replace(
                regex = TIMES,
                replacement = "*"
            )
            .replace(
                oldValue = "÷",
                newValue = "/"
            )
            .filterNot { it.isWhitespace() }

        val tokens = TOKEN
            .findAll(input = written)
            .map { it.value }
            .toList()

        val isAllRead = written.isNotEmpty() && tokens.sumOf { it.length } == written.length
        if (!isAllRead) return null

        return evaluated(postfix = postfixOf(tokens = tokens) ?: return null)?.takeIf {
            it.isFinite()
        }
    }

    /** [value] as a person writes it, never in scientific notation: whole when it is, else to four decimals. */
    fun written(value: Double): String {
        if (abs(value) >= WHOLE_LIMIT) return value.toString()

        val scaled = round(abs(value) * DECIMALS).toLong()
        val whole = scaled / DECIMALS

        val decimals = (scaled % DECIMALS)
            .toString()
            .padStart(
                length = DECIMAL_PLACES,
                padChar = '0'
            )
            .trimEnd('0')

        val sign = if (value < 0 && scaled > 0) "-" else ""

        return if (decimals.isEmpty()) "$sign$whole" else "$sign$whole.$decimals"
    }

    // The shunting yard: operators wait on a stack until one that binds no tighter comes.
    private fun postfixOf(tokens: List<String>): List<String>? {
        val output = mutableListOf<String>()
        val waiting = ArrayDeque<String>()
        var previous: String? = null

        tokens.forEach { token ->
            val isUnaryMinus = token == "-" && (
                previous == null ||
                    previous in PRECEDENCE ||
                    previous == "("
                )

            val operator = if (isUnaryMinus) NEGATE else token

            when {
                operator.first().isDigit() -> output += operator

                operator == "(" -> waiting.addLast(element = operator)

                operator == ")" -> {
                    while (waiting.lastOrNull() in PRECEDENCE) output += waiting.removeLast()
                    waiting.removeLastOrNull() ?: return null
                }

                // A minus before a number waits for what follows it: it takes nothing off the stack.
                operator == NEGATE -> waiting.addLast(element = operator)

                else -> {
                    while (bindsBefore(operator = operator, waiting = waiting.lastOrNull())) {
                        output += waiting.removeLast()
                    }

                    waiting.addLast(element = operator)
                }
            }

            previous = operator
        }

        if ("(" in waiting) return null

        return output + waiting.reversed()
    }

    private fun bindsBefore(
        operator: String,
        waiting: String?
    ): Boolean {
        val held = PRECEDENCE[waiting] ?: return false
        val coming = PRECEDENCE.getValue(key = operator)

        return held > coming || (held == coming && operator !in RIGHT)
    }

    private fun evaluated(postfix: List<String>): Double? {
        val values = ArrayDeque<Double>()

        postfix.forEach { token ->
            val number = token.toDoubleOrNull()

            when {
                number != null -> values.addLast(element = number)

                token == NEGATE -> {
                    val value = values.removeLastOrNull() ?: return null
                    values.addLast(element = -value)
                }

                else -> {
                    val right = values.removeLastOrNull() ?: return null
                    val left = values.removeLastOrNull() ?: return null

                    val result = when (token) {
                        "+" -> left + right
                        "-" -> left - right
                        "*" -> left * right
                        "/" -> left / right
                        else -> left.pow(x = right)
                    }

                    values.addLast(element = result)
                }
            }
        }

        return values.singleOrNull()
    }
}

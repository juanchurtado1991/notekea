package app.notekea.core.notes.domain

/** The backslash escapes of a double-quoted YAML text, read and written. */
internal object YamlEscapes {
    private const val FIRST_PRINTABLE = ' '
    private const val FIRST_SUPPLEMENTARY = 0x10000
    private const val HEX_ESCAPE = "\\x"
    private const val HEX_ESCAPE_DIGITS = 2
    private const val HEX_RADIX = 16
    private const val HIGH_SURROGATE_SHIFT = 10
    private const val LAST_CODE_POINT = 0x10FFFF
    private const val LOW_SURROGATE_MASK = 0x3FF
    private const val ZERO = '0'
    private val ESCAPE = Regex(pattern = "\\\\(?:x(\\p{XDigit}{2})|u(\\p{XDigit}{4})|U(\\p{XDigit}{8})|([\\s\\S]))")
    private val READ = mapOf(
        '0' to "\u0000",
        'a' to "\u0007",
        'b' to "\b",
        't' to "\t",
        'n' to "\n",
        'v' to "\u000B",
        'f' to "\u000C",
        'r' to "\r",
        'e' to "\u001B",
        ' ' to " ",
        '"' to "\"",
        '/' to "/",
        '\\' to "\\",
        'N' to "\u0085",
        '_' to " ",
        'L' to " ",
        'P' to " "
    )
    private val WRITTEN = mapOf(
        '\\' to "\\\\",
        '"' to "\\\"",
        '\n' to "\\n",
        '\r' to "\\r",
        '\t' to "\\t"
    )

    /** Whether [text] holds a line break or another character only an escape can write. */
    fun needsEscapes(
        text: String
    ): Boolean = text.any { it < FIRST_PRINTABLE }

    /** [text], the inside of a double-quoted YAML text, with its escapes undone; an unknown one stays as written. */
    fun decoded(
        text: String
    ): String = ESCAPE.replace(input = text) { match ->
        val named = match.groupValues[4].firstOrNull()
        if (named != null) {
            READ[named] ?: match.value
        } else {
            val digits = match.groupValues.subList(fromIndex = 1, toIndex = 4).first { it.isNotEmpty() }
            digits.toIntOrNull(radix = HEX_RADIX)?.let { textOf(codePoint = it) } ?: match.value
        }
    }

    /** [text] as the inside of a double-quoted YAML text. */
    fun encoded(
        text: String
    ): String = buildString {
        text.forEach { char ->
            when {
                char in WRITTEN -> append(WRITTEN[char])

                char < FIRST_PRINTABLE -> {
                    append(HEX_ESCAPE)
                    append(char.code.toString(radix = HEX_RADIX).padStart(length = HEX_ESCAPE_DIGITS, padChar = ZERO))
                }

                else -> append(char)
            }
        }
    }

    /** The character of [codePoint], two for one outside the first plane; null past the last. */
    private fun textOf(
        codePoint: Int
    ): String? = when {
        codePoint !in 0..LAST_CODE_POINT -> null

        codePoint < FIRST_SUPPLEMENTARY -> Char(code = codePoint).toString()

        else -> {
            val offset = codePoint - FIRST_SUPPLEMENTARY
            val high = Char.MIN_HIGH_SURROGATE + (offset shr HIGH_SURROGATE_SHIFT)
            val low = Char.MIN_LOW_SURROGATE + (offset and LOW_SURROGATE_MASK)
            charArrayOf(high, low).concatToString()
        }
    }
}

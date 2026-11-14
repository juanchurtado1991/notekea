package app.notekea.chat.domain.tool

/**
 * A tool call read from the JSON the model wrote under the registry's grammar: one flat object, the tool named in
 * [ToolRegistry.NAMED], each argument a text or a list of texts (kept one item a line). Anything else, or a call
 * cut short, is no call.
 */
internal object ToolCallJson {
    private val BETWEEN = setOf(' ', '\n', '\t', '\r', ',')
    private const val ESCAPES = "\"\\/"
    private const val UNICODE_DIGITS = 4
    private const val HEX = 16
    private val CONTROLS =
        mapOf('n' to '\n', 't' to '\t', 'r' to '\r', 'b' to '\b', 'f' to '\u000C')

    fun read(json: String): ToolCall? {
        val fields = fields(json = json) ?: return null
        val name = fields[ToolRegistry.NAMED]?.takeIf { it.isNotBlank() } ?: return null

        return ToolCall(
            name = name.trim(),
            arguments = fields - ToolRegistry.NAMED
        )
    }

    /** The fields of the flat object [json] is, read in one pass with a cursor; null when it is not one, whole. */
    fun fields(json: String): Map<String, String>? {
        val fields = linkedMapOf<String, String>()
        var at = json.indexOf(char = '{') + 1
        var isCut = false
        if (at == 0) return null

        fun skipSpace() {
            while (json.getOrNull(index = at) in BETWEEN) at++
        }

        fun text(): String? {
            if (json.getOrNull(index = at) != '"') return null
            val read = StringBuilder()
            at++

            while (at < json.length && json[at] != '"') {
                if (json[at] == '\\') {
                    at += escaped(
                        json = json,
                        at = at,
                        into = read
                    )
                } else {
                    read.append(json[at])
                    at++
                }
            }

            // No closing quote: the reply ran out of room in the middle of a text.
            if (at >= json.length) isCut = true
            at++

            return read.toString()
        }

        fun list(): String {
            val items = mutableListOf<String>()
            at++
            skipSpace()

            while (at < json.length && json[at] != ']') {
                items += text() ?: literal(
                    json = json,
                    from = at
                ).also { at += it.length.coerceAtLeast(1) }

                skipSpace()
            }

            at++

            return items.joinToString(separator = "\n")
        }

        while (true) {
            skipSpace()
            val key = text() ?: break
            skipSpace()
            if (json.getOrNull(index = at) != ':') return null
            at++
            skipSpace()

            fields[key] = when (json.getOrNull(index = at)) {
                '"' -> text().orEmpty()

                '[' -> list()

                else -> literal(
                    json = json,
                    from = at
                ).also { at += it.length.coerceAtLeast(1) }
            }
        }

        val isWhole = !isCut && json.getOrNull(index = at) == '}'

        return fields.takeIf { isWhole }
    }

    // A number, true, false or null, as written.
    private fun literal(
        json: String,
        from: Int
    ): String = json
        .drop(n = from)
        .takeWhile {
            it !in ",]}" && !it.isWhitespace()
        }

    // The character an escape at [at] stands for, added to [into]; how many characters of [json] it took.
    private fun escaped(
        json: String,
        at: Int,
        into: StringBuilder
    ): Int {
        val mark = json.getOrNull(index = at + 1) ?: return 1

        val code = json
            .drop(n = at + 2)
            .take(n = UNICODE_DIGITS)
            .toIntOrNull(radix = HEX)

        val isUnicode = mark == 'u' && code != null

        when {
            isUnicode -> into.append(code.toChar())
            mark in ESCAPES -> into.append(mark)
            else -> into.append(CONTROLS[mark] ?: mark)
        }

        return if (isUnicode) 2 + UNICODE_DIGITS else 2
    }
}

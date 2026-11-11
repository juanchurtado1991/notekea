package app.notekea.editor.data

/** The text fields of YouTube's oEmbed answer, a flat JSON object, read without a JSON library. */
internal object OEmbedJson {
    private const val HEX = 16
    private val ESCAPE = Regex(pattern = "\\\\(u[0-9a-fA-F]{4}|.)")

    /** The text of the field [name] in [json], its escapes undone; null when it is missing or blank. */
    fun stringOf(
        json: String,
        name: String
    ): String? = Regex(pattern = "\"$name\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
        .find(input = json)
        ?.groupValues
        ?.get(index = 1)
        ?.let { unescaped(text = it) }
        ?.takeIf { it.isNotBlank() }

    // JSON's escapes: \" \\ \/ \n \t and \uXXXX.
    private fun unescaped(text: String): String = ESCAPE
        .replace(input = text) { match ->
            val escape = match.groupValues[1]

            when {
                escape.startsWith(prefix = "u") ->
                    escape
                        .drop(n = 1)
                        .toInt(radix = HEX)
                        .toChar()
                        .toString()

                escape == "n" -> "\n"

                escape == "t" -> "\t"

                else -> escape
            }
        }
}

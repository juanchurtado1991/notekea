package app.notekea.core.notes.data

/**
 * A text field set in a flat JSON object as written, every other field and its spacing kept: Obsidian's settings
 * files, which Notekea changes one field at a time and must never rewrite (5.2).
 */
internal object JsonFields {
    private const val EMPTY_OBJECT = "{}"

    /** [json] with its top-level field [name] set to the text [value]; a new object when [json] is blank. */
    fun withText(
        json: String,
        name: String,
        value: String
    ): String {
        val written = "\"${escaped(text = name)}\": \"${escaped(text = value)}\""
        val text = json.trim().ifEmpty { EMPTY_OBJECT }

        val field = Regex(
            pattern = "\"${Regex.escape(literal = name)}\"\\s*:\\s*" +
                "(\"(?:[^\"\\\\]|\\\\.)*\"|null|true|false|-?[0-9][0-9.eE+-]*)"
        )

        field.find(input = text)?.let {
            return text.replaceRange(
                range = it.range,
                replacement = written
            )
        }

        val end = text.lastIndexOf(char = '}')
        if (end < 0) return "{\n  $written\n}"

        val isEmpty = text
            .substring(
                startIndex = 0,
                endIndex = end
            )
            .trimEnd()
            .endsWith(suffix = "{")

        val separator = if (isEmpty) "\n  " else ",\n  "

        return text.substring(
            startIndex = 0,
            endIndex = end
        ).trimEnd() + separator + written + "\n" +
            text.substring(startIndex = end)
    }

    private fun escaped(text: String): String = text
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
}

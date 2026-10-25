package app.notekea.notes.domain

/**
 * Text for searching: lowercase and without accents (`Café` → `cafe`), one character for one character, so a position
 * found in the folded text is the same position in the original and highlights land on the right letters.
 */
class TextFolder {
    fun fold(
        text: String
    ): String = buildString(capacity = text.length) {
        text.forEach { char ->
            val lower = char.lowercaseChar()
            append(UNACCENTED[lower] ?: lower)
        }
    }

    companion object {
        private val UNACCENTED: Map<Char, Char> = mapOf(
            'a' to "àáâãäåāăą",
            'c' to "çćĉċč",
            'e' to "èéêëēĕėęě",
            'i' to "ìíîïĩīĭįı",
            'n' to "ñńņňŉ",
            'o' to "òóôõöøōŏő",
            'u' to "ùúûüũūŭůűų",
            'y' to "ýÿŷ",
            's' to "śŝşš",
            'z' to "źżž"
        ).flatMap { (plain, accented) -> accented.map { it to plain } }.toMap()
    }
}

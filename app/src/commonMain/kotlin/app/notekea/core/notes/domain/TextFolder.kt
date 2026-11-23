package app.notekea.core.notes.domain

/**
 * Text for searching and matching words: lowercase and without accents (`Café` → `cafe`), one character for one
 * character, so a position found in the folded text is the same position in the original and highlights land on the
 * right letters. Every reader that matches words written with or without accents folds them here.
 */
object TextFolder {
    private val UNACCENTED: Map<Char, Char> = mapOf(
        'a' to "àáâãäåāăą",
        'c' to "çćĉċč",
        'd' to "ďđ",
        'e' to "èéêëēĕėęě",
        'g' to "ĝğġģ",
        'i' to "ìíîïĩīĭįı",
        'l' to "ĺļľŀł",
        'n' to "ñńņňŉ",
        'o' to "òóôõöøōŏő",
        'r' to "ŕŗř",
        'u' to "ùúûüũūŭůűų",
        'y' to "ýÿŷ",
        's' to "śŝşš",
        't' to "ţťŧ",
        'z' to "źżž"
    ).flatMap { (plain, accented) ->
        accented.map {
            it to plain
        }
    }.toMap()

    fun fold(text: String): String = buildString(capacity = text.length) {
        text.forEach { char ->
            val lower = char.lowercaseChar()
            append(UNACCENTED[lower] ?: lower)
        }
    }
}

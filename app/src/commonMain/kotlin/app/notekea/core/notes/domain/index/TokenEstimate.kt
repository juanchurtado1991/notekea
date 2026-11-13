package app.notekea.core.notes.domain.index

/**
 * How many tokens a text is for the embedding model, estimated without its tokenizer: one every two characters, or
 * every character in scripts written without spaces, and at least two per word, whichever is larger. It never
 * undercounts the bundled model's tokenizer (`docs/spikes/2026-11-01-embedding-models.md`).
 */
object TokenEstimate {
    fun of(text: String): Int {
        val unspaced = text.count { isUnspaced(character = it) }

        val spaced = text.length - unspaced

        val byCharacters = unspaced +
            (spaced * TENTHS + CHARACTERS_PER_TOKEN_TENTHS - 1) / CHARACTERS_PER_TOKEN_TENTHS

        val byWords = WORD.findAll(input = text).count() * TOKENS_PER_WORD

        return maxOf(byCharacters, byWords)
    }

    private fun isUnspaced(character: Char): Boolean =
        character.code >= FIRST_UNSPACED && UNSPACED_SCRIPTS.any {
            character.code in it
        }

    private const val CHARACTERS_PER_TOKEN_TENTHS = 20
    private const val FIRST_UNSPACED = 0x0E00
    private const val TENTHS = 10
    private const val TOKENS_PER_WORD = 2
    private val WORD = Regex(pattern = "\\S+")

    // Thai and Lao, Myanmar, Khmer, Japanese kana, Chinese and Japanese ideographs, Hangul, half-width kana.
    private val UNSPACED_SCRIPTS = listOf(
        FIRST_UNSPACED..0x0EFF,
        0x1000..0x109F,
        0x1780..0x17FF,
        0x3040..0x30FF,
        0x3400..0x4DBF,
        0x4E00..0x9FFF,
        0xAC00..0xD7A3,
        0xF900..0xFAFF,
        0xFF66..0xFF9F
    )
}

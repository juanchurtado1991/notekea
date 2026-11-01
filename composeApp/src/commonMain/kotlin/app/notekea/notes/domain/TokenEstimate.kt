package app.notekea.notes.domain

/**
 * How many tokens a text is for the embedding model, estimated without its tokenizer (`ROADMAP.md` 2.1): one token
 * every two characters and at least two per word, whichever is larger, so a chunk is never larger than it is counted.
 * Measured against the bundled multilingual model's tokenizer on every chunk of `smoke-data`
 * (`docs/spikes/2026-11-01-embedding-models.md`): it cuts code and rare words into many pieces, down to 2.13
 * characters a token, and this estimate never undercounts (`ChunkTokensRealModelTest`).
 */
object TokenEstimate {
    fun of(
        text: String
    ): Int {
        val byCharacters = (text.length * TENTHS + CHARACTERS_PER_TOKEN_TENTHS - 1) / CHARACTERS_PER_TOKEN_TENTHS
        val byWords = WORD.findAll(input = text).count() * TOKENS_PER_WORD
        return maxOf(byCharacters, byWords)
    }

    private const val CHARACTERS_PER_TOKEN_TENTHS = 20
    private const val TENTHS = 10
    private const val TOKENS_PER_WORD = 2
    private val WORD = Regex(pattern = "\\S+")
}

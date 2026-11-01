package app.notekea.notes.domain

/**
 * How many tokens a text is for an embedding model, estimated without its tokenizer (`ROADMAP.md` 2.1): about four
 * characters a token for prose and more tokens than words for code and short words, whichever is larger, so a chunk
 * is never larger than it is counted. 2.2 compares it with the model's own tokenizer.
 */
object TokenEstimate {
    fun of(
        text: String
    ): Int {
        val byCharacters = (text.length + CHARACTERS_PER_TOKEN - 1) / CHARACTERS_PER_TOKEN
        val words = WORD.findAll(input = text).count()
        val byWords = (words * TOKENS_PER_WORD_TENTHS + TENTHS - 1) / TENTHS
        return maxOf(byCharacters, byWords)
    }

    private const val CHARACTERS_PER_TOKEN = 4

    // Four tokens for every three words: a word is a token or two in English and Spanish alike.
    private const val TOKENS_PER_WORD_TENTHS = 13
    private const val TENTHS = 10
    private val WORD = Regex(pattern = "\\S+")
}

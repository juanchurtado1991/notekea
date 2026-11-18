package app.notekea.chat.domain.loop

/**
 * The words the frame's candidates are matched by (`FrameCandidates`): letters and digits, accents folded, of four
 * letters or more, or of three when written with a capital (a name: Ana, Leo). Two words are the same word when one
 * starts the other and the shorter has four letters or more: "cita" and "citas", "invoice" and "invoices".
 */
internal object FrameWords {
    fun of(text: String): Set<String> = WORD
        .findAll(input = text)
        .map { it.value }
        .filter {
            it.length >= LONG || (it.length >= NAME && it.first().isUpperCase())
        }
        .map { folded(text = it) }
        .toSet()

    fun isSame(
        one: String,
        other: String
    ): Boolean {
        if (one == other) return true

        val (shorter, longer) = if (one.length <= other.length) one to other else other to one

        return shorter.length >= LONG && longer.startsWith(prefix = shorter)
    }

    private fun folded(text: String): String = text
        .lowercase()
        .map {
            ACCENTS[it] ?: it
        }
        .joinToString(separator = "")

    private val WORD = Regex(pattern = "[\\p{L}\\p{N}]+")

    private const val LONG = 4
    private const val NAME = 3

    private val ACCENTS = mapOf(
        'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n',
        'à' to 'a', 'è' to 'e', 'ç' to 'c', 'ã' to 'a', 'õ' to 'o', 'â' to 'a', 'ê' to 'e',
        'ô' to 'o'
    )
}

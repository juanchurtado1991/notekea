package app.notekea.chat.domain.loop

/**
 * The words the frame's candidates are matched by (`FrameCandidates`): letters and digits, accents folded, of four
 * letters or more, or of three when written with a capital (a name: Ana, Leo). Two words are the same word when one
 * starts the other, two letters longer at most, and the shorter has four letters or more: "cita" and "citas".
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

    /** The words of three letters [text] writes in any case: one names a note only as a word of its path, "lab". */
    fun shortOf(text: String): Set<String> = WORD
        .findAll(input = text)
        .map { it.value }
        .filter {
            it.length == NAME
        }
        .map { folded(text = it) }
        .toSet()

    /** The names [text] writes: its words of three letters or more with a capital, and its numbers and codes whole. */
    fun namesOf(text: String): Set<String> = WORD
        .findAll(input = text)
        .map { it.value }
        .filter {
            it.length >= NAME && it.first().isUpperCase()
        }
        .map { folded(text = it) }
        .toSet() + codesOf(text = text)

    /** The numbers and codes [text] writes, each whole: "11:00" is not "09:00", nor "2026-038" another of 2026. */
    fun codesOf(text: String): Set<String> = CODE
        .findAll(input = text)
        .map { folded(text = it.value) }
        .filter { code ->
            code.any { it.isDigit() }
        }
        .toSet()

    /** The numbers [text] writes, as written: a number names a note only exactly, "47" is no "470". */
    fun numbersOf(text: String): Set<String> = NUMBER
        .findAll(input = text)
        .map { it.value }
        .toSet()

    fun isSame(
        one: String,
        other: String
    ): Boolean {
        if (one == other) return true

        val (shorter, longer) = if (one.length <= other.length) one to other else other to one

        // An inflection, not another word: "call" is no "callouts", "note" no "Notekea" (the run of 2026-11-17).
        val isInflection = longer.length - shorter.length <= INFLECTION

        return shorter.length >= LONG &&
            isInflection &&
            longer.startsWith(prefix = shorter)
    }

    private fun folded(text: String): String = text
        .lowercase()
        .map {
            ACCENTS[it] ?: it
        }
        .joinToString(separator = "")

    private val WORD = Regex(pattern = "[\\p{L}\\p{N}]+")
    private val CODE = Regex(pattern = "[\\p{L}\\p{N}]+(?:[-:./,][\\p{L}\\p{N}]+)*")
    private val NUMBER = Regex(pattern = "(?<![\\p{L}\\p{N}])\\p{N}+(?![\\p{L}\\p{N}])")

    private const val LONG = 4
    private const val NAME = 3
    private const val INFLECTION = 2

    private val ACCENTS = mapOf(
        'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n',
        'à' to 'a', 'è' to 'e', 'ç' to 'c', 'ã' to 'a', 'õ' to 'o', 'â' to 'a', 'ê' to 'e',
        'ô' to 'o'
    )
}

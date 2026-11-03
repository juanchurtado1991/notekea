package app.notekea.notes.domain

/**
 * A short fingerprint of a note's text (`ROADMAP.md` 2.3): the same text always gives the same one, and any change
 * gives another but with a chance too small to matter (64-bit FNV-1a, with the text's length). The index compares it
 * to know whether a note saved again changed; it is not meant to resist anyone forging one.
 */
object ContentHash {
    fun of(
        text: String
    ): String {
        var hash = OFFSET_BASIS
        text.forEach { char ->
            hash = (hash xor char.code.toLong()) * PRIME
        }
        return hash.toULong().toString(radix = HEX) + LENGTH_SEPARATOR + text.length.toString(radix = HEX)
    }

    private const val OFFSET_BASIS = -0x340d631b7bdddcdbL
    private const val PRIME = 0x100000001b3L
    private const val HEX = 16
    private const val LENGTH_SEPARATOR = "-"
}

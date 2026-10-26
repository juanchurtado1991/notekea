package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns

/**
 * Orders names the way file managers do: letter case is ignored and numbers compare by value, so `Note 2` comes
 * before `Note 10` and `2026-10-9` before `2026-10-10`. Names equal except for case keep a stable, case-sensitive
 * order.
 */
class NaturalNameComparator : Comparator<String> {
    override fun compare(
        a: String,
        b: String
    ): Int {
        val chunksA = chunksOf(name = a)
        val chunksB = chunksOf(name = b)
        chunksA.zip(chunksB).forEach { (chunkA, chunkB) ->
            val result = compareChunks(a = chunkA, b = chunkB)
            if (result != 0) return result
        }
        return compareValues(chunksA.size, chunksB.size).takeIf { it != 0 } ?: a.compareTo(other = b)
    }

    private fun chunksOf(
        name: String
    ): List<String> = CHUNK.findAll(input = name).map { it.value }.toList()

    private fun compareChunks(
        a: String,
        b: String
    ): Int {
        val areNumbers = a.first().isDigit() && b.first().isDigit()
        if (!areNumbers) return a.lowercase().compareTo(other = b.lowercase())
        val digitsA = a.trimStart(StringPatterns.ZERO_CHAR)
        val digitsB = b.trimStart(StringPatterns.ZERO_CHAR)
        // Comparing lengths first keeps numbers of any size exact, with no overflow.
        return compareValuesBy(digitsA, digitsB, { it.length }, { it })
    }

    companion object {
        private val CHUNK = Regex(pattern = "\\d+|\\D+")
    }
}

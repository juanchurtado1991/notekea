package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns

/**
 * Orders names the way file managers do: letter case is ignored and numbers compare by value, so `Note 2` comes
 * before `Note 10` and `2026-10-9` before `2026-10-10`. Names equal except for case keep a stable, case-sensitive
 * order. A name is read in runs of digits and of other characters, in place: sorting a large vault compares names
 * thousands of times, so no comparison builds lists or matches a pattern.
 */
class NaturalNameComparator : Comparator<String> {
    override fun compare(
        a: String,
        b: String
    ): Int {
        var startA = 0
        var startB = 0
        while (startA < a.length && startB < b.length) {
            val endA = chunkEnd(name = a, start = startA)
            val endB = chunkEnd(name = b, start = startB)
            val result = compareChunks(a = a, startA = startA, endA = endA, b = b, startB = startB, endB = endB)
            if (result != 0) return result
            startA = endA
            startB = endB
        }
        // Equal so far: the name with runs left comes after, and two equal names keep a case-sensitive order.
        return compareValues(startA < a.length, startB < b.length).takeIf { it != 0 } ?: a.compareTo(other = b)
    }

    /** Where the run of digits, or of other characters, that starts at [start] ends. */
    private fun chunkEnd(
        name: String,
        start: Int
    ): Int {
        val isDigits = isAsciiDigit(char = name[start])
        var end = start + 1
        while (end < name.length && isAsciiDigit(char = name[end]) == isDigits) end++
        return end
    }

    private fun compareChunks(
        a: String,
        startA: Int,
        endA: Int,
        b: String,
        startB: Int,
        endB: Int
    ): Int {
        val areNumbers = a[startA].isDigit() && b[startB].isDigit()
        if (!areNumbers) return compareText(a = a, startA = startA, endA = endA, b = b, startB = startB, endB = endB)
        val digitsA = a.substring(startIndex = startA, endIndex = endA).trimStart(StringPatterns.ZERO_CHAR)
        val digitsB = b.substring(startIndex = startB, endIndex = endB).trimStart(StringPatterns.ZERO_CHAR)
        // Comparing lengths first keeps numbers of any size exact, with no overflow.
        return compareValuesBy(digitsA, digitsB, { it.length }, { it })
    }

    /** Two runs of text ignoring case: character by character when both are ASCII, as lowercasing them would. */
    private fun compareText(
        a: String,
        startA: Int,
        endA: Int,
        b: String,
        startB: Int,
        endB: Int
    ): Int {
        val isAscii =
            (startA until endA).all { a[it].code < ASCII_END } && (startB until endB).all { b[it].code < ASCII_END }
        if (!isAscii) {
            val textA = a.substring(startIndex = startA, endIndex = endA).lowercase()
            return textA.compareTo(other = b.substring(startIndex = startB, endIndex = endB).lowercase())
        }
        val length = minOf(endA - startA, endB - startB)
        for (offset in 0 until length) {
            val result = a[startA + offset].lowercaseChar().compareTo(other = b[startB + offset].lowercaseChar())
            if (result != 0) return result
        }
        return (endA - startA).compareTo(other = endB - startB)
    }

    private fun isAsciiDigit(
        char: Char
    ): Boolean = char in '0'..'9'

    companion object {
        private const val ASCII_END = 128
    }
}

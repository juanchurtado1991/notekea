package app.notekea.notes.domain

/**
 * Fuzzy matching for the quick switcher (`ROADMAP.md` 1.6): the letters of the search must appear in the text in
 * order, not necessarily together (`pfag` matches "Pasta e fagioli"). Letter case and accents are ignored (`cafe`
 * matches "Café"), and spaces in the search are skipped. Among the ways the letters can match, the best is chosen:
 * letters at the start of the text or of a word and letters next to each other score higher, and every skipped
 * character scores a little lower (up to a point before the first letter), so for `are` "Arepas" ranks above
 * "Café con leche y arepas", which ranks above "Prepare dough".
 */
class FuzzyMatcher {
    fun match(
        query: String,
        text: String
    ): FuzzyMatch? {
        val pattern = folded(text = query).filterNot { it.isWhitespace() }
        if (pattern.isEmpty()) return FuzzyMatch.EVERYTHING
        val target = folded(text = text)
        if (!isSubsequence(pattern = pattern, target = target)) return null
        return bestMatch(pattern = pattern, target = target, text = text)
    }

    /**
     * Scores every place each letter of the pattern can match, keeping the best way to reach it (dynamic programming
     * in `pattern × text` steps): `scores[i][j]` is the best score with letter `i` matched at character `j`.
     */
    private fun bestMatch(
        pattern: String,
        target: String,
        text: String
    ): FuzzyMatch {
        val scores = Array(size = pattern.length) { IntArray(size = target.length) { NO_MATCH } }
        val previous = Array(size = pattern.length) { IntArray(size = target.length) { -1 } }
        target.indices.filter { target[it] == pattern[0] }.forEach { j ->
            scores[0][j] = MATCH + bonusAt(text = text, index = j) - minOf(j, MAX_LEADING_GAP) * LEADING_GAP
        }
        for (i in 1 until pattern.length) {
            fillRow(row = i, pattern = pattern, target = target, text = text, scores = scores, previous = previous)
        }
        val last = scores.last()
        val finalScores = last.indices.filter { last[it] != NO_MATCH }.associateWith { end ->
            last[end] - (target.length - 1 - end) * TRAILING_GAP
        }
        val end = finalScores.maxBy { it.value }.key
        val matched = generateSequence(seed = pattern.lastIndex to end) { (i, j) ->
            if (i == 0) null else i - 1 to previous[i][j]
        }.map { it.second }.toList().reversed()
        return FuzzyMatch(score = finalScores.getValue(key = end), ranges = runsOf(indices = matched))
    }

    /** Letter [row] of the pattern after the letter before it: right next to it, or after a gap. */
    private fun fillRow(
        row: Int,
        pattern: String,
        target: String,
        text: String,
        scores: Array<IntArray>,
        previous: Array<IntArray>
    ) {
        val before = scores[row - 1]
        // The best earlier match two or more characters back, offset so the gap penalty is one subtraction.
        var bestGapped = NO_MATCH
        var bestGappedAt = -1
        for (j in row until target.length) {
            val gappedFrom = j - 2
            if (gappedFrom >= 0 && before[gappedFrom] != NO_MATCH) {
                val offset = before[gappedFrom] + gappedFrom * GAP
                if (offset > bestGapped) {
                    bestGapped = offset
                    bestGappedAt = gappedFrom
                }
            }
            if (target[j] != pattern[row]) continue
            val adjacent = before[j - 1].takeIf { it != NO_MATCH }?.plus(CONSECUTIVE)
            val gapped = bestGapped.takeIf { it != NO_MATCH }?.minus((j - 1) * GAP)
            val best = maxOf(adjacent ?: NO_MATCH, gapped ?: NO_MATCH)
            if (best == NO_MATCH) continue
            scores[row][j] = best + MATCH + bonusAt(text = text, index = j)
            previous[row][j] = if (adjacent != null && adjacent >= (gapped ?: NO_MATCH)) j - 1 else bestGappedAt
        }
    }

    /** Extra points for a letter that starts the text, a word (after a space, `-`, `/`…) or a camelCase hump. */
    private fun bonusAt(
        text: String,
        index: Int
    ): Int {
        if (index == 0) return START
        val before = text[index - 1]
        val current = text[index]
        return when {
            !before.isLetterOrDigit() -> WORD_START
            before.isLowerCase() && current.isUpperCase() -> CAMEL_HUMP
            else -> 0
        }
    }

    private fun isSubsequence(
        pattern: String,
        target: String
    ): Boolean {
        var next = 0
        for (char in target) {
            if (char == pattern[next]) next++
            if (next == pattern.length) return true
        }
        return false
    }

    private fun runsOf(
        indices: List<Int>
    ): List<IntRange> = indices.fold(initial = mutableListOf<IntRange>()) { runs, index ->
        val last = runs.lastOrNull()
        if (last != null && last.last + 1 == index) runs[runs.lastIndex] = last.first..index else runs += index..index
        runs
    }

    /** Lowercase without accents, one character for one character so matched positions stay those of the text. */
    private fun folded(
        text: String
    ): String = buildString(capacity = text.length) {
        text.forEach { char ->
            val lower = char.lowercaseChar()
            append(UNACCENTED[lower] ?: lower)
        }
    }

    companion object {
        private const val MATCH = 16
        private const val START = 12
        private const val WORD_START = 10
        private const val CAMEL_HUMP = 8
        private const val CONSECUTIVE = 14
        private const val GAP = 2
        private const val LEADING_GAP = 1

        /** Capped, so a word that starts later in a long title still beats letters inside an earlier word. */
        private const val MAX_LEADING_GAP = 8
        private const val TRAILING_GAP = 1
        private const val NO_MATCH = Int.MIN_VALUE
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

package app.notekea.ai.data

/**
 * Takes the Markdown fence off a reply held to a schema, as it streams: Gemma 4 writes the JSON in ` ```json ` after
 * its thought (`<|channel>thought<channel|>`, empty with thinking off); Qwen3.5 writes it bare, which passes as it is.
 */
internal class JsonFence {
    private val held = StringBuilder()
    private var isOpen = false

    /** What [piece] adds to the reply once the fence is off: nothing while it may still be the fence's own text. */
    fun taken(piece: String): String {
        held.append(piece)

        val isSettled = isOpen || opened()

        if (!isSettled) return ""

        // A fence's end is backticks and blanks: the reply's tail of them waits until it is known not to be one.
        val kept = held.takeLastWhile { it.isFenceEnd() }.length
        val sent = held.substring(0, held.length - kept)

        held.delete(0, held.length - kept)

        return sent
    }

    /** What is left once the model stops writing: the reply's own last characters, without the fence's end. */
    fun ended(): String {
        val left = held.toString()

        held.clear()

        return if (isOpen) {
            left
                .trimEnd()
                .removeSuffix(suffix = FENCE)
                .trimEnd()
        } else {
            left.trim()
        }
    }

    // Whether the reply's start is settled: no fence, or a whole first line of one (dropped with its language), once
    // a thought before it is whole and dropped. The cards read none of 172 replies that began with an empty thought
    // (their set, 2026-11-26).
    private fun opened(): Boolean {
        val written = held.trimStart()
        val isThought = written.startsWith(prefix = THOUGHT) || THOUGHT.startsWith(prefix = written)
        val thoughtEnd = written.indexOf(string = THOUGHT_END)

        if (isThought && thoughtEnd < 0) return false

        val start = if (isThought) {
            written
                .substring(startIndex = thoughtEnd + THOUGHT_END.length)
                .trimStart()
        } else {
            written
        }

        val isFenced = start.startsWith(prefix = FENCE)
        val line = start.indexOf(char = '\n')
        val isFirstLineCut = isFenced && line < 0
        val isUnsettled = FENCE.startsWith(prefix = start) || isFirstLineCut

        if (isUnsettled) return false

        held.clear()
        held.append(if (isFenced) start.substring(startIndex = line + 1) else start)
        isOpen = true

        return true
    }

    private fun Char.isFenceEnd(): Boolean = this == MARK || isWhitespace()

    private companion object {
        const val MARK = '`'
        const val FENCE = "```"
        const val THOUGHT = "<|channel>"
        const val THOUGHT_END = "<channel|>"
    }
}

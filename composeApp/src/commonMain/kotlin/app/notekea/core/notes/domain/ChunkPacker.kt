package app.notekea.core.notes.domain

/**
 * Packs ranges of a note's body, in order, into chunks of at most [ChunkSize.TARGET] (a range larger than it is a
 * chunk of its own), and gives each chunk after the first the previous one's last sentence or line when it is at most
 * [ChunkSize.OVERLAP] and the chunk still fits [ChunkSize.MAX] ([overlapped]), so a sentence cut between two chunks is whole in one of them.
 */
object ChunkPacker {
    fun pack(
        body: String,
        ranges: List<IntRange>
    ): List<IntRange> {
        val chunks = mutableListOf<IntRange>()
        ranges.forEach { range ->
            val last = chunks.lastOrNull()
            if (last != null && ChunkSize.of(body = body, range = last.first..range.last) <= ChunkSize.TARGET) {
                chunks[chunks.lastIndex] = last.first..range.last
            } else {
                chunks += range
            }
        }
        return chunks
    }

    /** [chunks] (consecutive ranges of one section), each after the first starting with the end of the one before. */
    fun overlapped(
        body: String,
        chunks: List<IntRange>
    ): List<IntRange> = chunks.mapIndexed { index, chunk ->
        if (index == 0) return@mapIndexed chunk
        val previous = chunks[index - 1]
        val start = lastUnitStart(body = body, range = previous)
        val fits = ChunkSize.of(body = body, range = start..previous.last) <= ChunkSize.OVERLAP &&
            ChunkSize.of(body = body, range = start..chunk.last) <= ChunkSize.MAX
        if (fits) start..chunk.last else chunk
    }

    /** Where the last sentence or line of [range] starts. */
    private fun lastUnitStart(
        body: String,
        range: IntRange
    ): Int {
        val text = body.substring(range = range).trimEnd()
        val lineStart = text.lastIndexOf(char = '\n') + 1
        val sentenceStart = SENTENCE_END.findAll(input = text).map { it.range.last + 1 }
            .filter { it < text.length }.lastOrNull() ?: 0
        val start = maxOf(lineStart, sentenceStart)
        return range.first + start + text.substring(startIndex = start).takeWhile { it.isWhitespace() }.length
    }

    private val SENTENCE_END = Regex(pattern = "[.!?]\\s+")
}

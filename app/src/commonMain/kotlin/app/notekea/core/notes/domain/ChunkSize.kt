package app.notekea.core.notes.domain

/**
 * The sizes of a note's chunks, in estimated tokens ([TokenEstimate], `ROADMAP.md` 2.1): sections shorter than [MIN]
 * join the next ones, chunks are packed up to [TARGET], none is larger than [MAX], and a chunk of a long section starts
 * with the previous one's last sentence or line when it is at most [OVERLAP].
 */
object ChunkSize {
    const val MIN = 200
    const val TARGET = 300
    const val MAX = 400
    const val OVERLAP = 60

    /** The tokens of [range] of [body]. */
    fun of(
        body: String,
        range: IntRange
    ): Int = TokenEstimate.of(text = body.substring(range = range))
}

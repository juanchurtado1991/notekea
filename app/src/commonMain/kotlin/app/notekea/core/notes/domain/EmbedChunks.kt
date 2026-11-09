package app.notekea.core.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.StringPatterns
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.TextEmbedder

/*
 * How a note's chunks reach the embedding model, for the search index and for Related alike: each with the note's
 * title and headings in front, which give it its subject. The chunks fit the model by the estimate; the words in
 * front get what room is left, since a chunk's text matters more. What the model still refuses is a chunk it counts
 * longer than the estimate: that chunk is left out, and the rest of its note still found.
 */

private const val HEADING_JOIN = " › "

/** [chunks] with their vectors, without those too long; [EmbeddingError.TextTooLong] when none can be read. */
suspend fun embedChunks(
    textEmbedder: TextEmbedder,
    title: String,
    chunks: List<NoteChunk>
): Outcome<List<Pair<NoteChunk, FloatArray>>, EmbeddingError> {
    var kept = chunks

    while (true) {
        val texts = kept.map {
            embeddingText(
                title = title,
                chunk = it,
                maxTokens = textEmbedder.maxTokens
            )
        }

        when (val embedded = textEmbedder.embed(texts = texts)) {
            is Outcome.Success -> return Outcome.Success(value = kept.zip(other = embedded.value))

            is Outcome.Failure -> {
                val error = embedded.error

                if (error !is EmbeddingError.TextTooLong || kept.size == 1) {
                    return Outcome.Failure(error = error)
                }

                kept = kept.filterIndexed { index, _ ->
                    index != error.index
                }
            }
        }
    }
}

/** [chunk] after its [title] and headings, those cut to what [maxTokens] leaves: a character is at most a token. */
private fun embeddingText(
    title: String,
    chunk: NoteChunk,
    maxTokens: Int
): String {
    val heading = (listOf(title) + chunk.headingPath).joinToString(separator = HEADING_JOIN)
    val room = maxTokens - chunk.tokens - 1

    val shown = if (TokenEstimate.of(text = heading) <= room) {
        heading
    } else {
        heading.take(n = (room - 1).coerceAtLeast(0))
    }

    return shown + StringPatterns.NEW_LINE + chunk.text
}

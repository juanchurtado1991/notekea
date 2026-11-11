package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.TextEmbedder

/**
 * The pages of a section most like the page pasted, first: by their meaning, with the embedding model the notes are
 * searched with, from what the site says each is ([SectionLinks.about]) or the words of its address.
 */
class SectionRanker(private val textEmbedder: TextEmbedder) {
    /** [links]' addresses, the closest to [page] first; null when the model cannot say. */
    suspend fun ranked(
        page: FetchedPage,
        links: SectionLinks
    ): List<String>? {
        val query = "${titleOf(page = page)} ${SectionLinks.wordsOf(url = page.url)}".trim()
        val texts = links.urls.map { links.aboutOf(url = it) }

        val vectors = (textEmbedder.embed(texts = listOf(query) + texts) as? Outcome.Success)
            ?.value
            ?: return null

        val asked = vectors.first()

        return links
            .urls
            .zip(vectors.drop(n = 1))
            .sortedByDescending { (_, vector) ->
                asked.indices.sumOf { (asked[it] * vector[it]).toDouble() }
            }
            .map { it.first }
    }

    private fun titleOf(page: FetchedPage): String = TITLE
        .find(input = page.html)
        ?.groupValues
        ?.get(1)
        ?.trim()
        .orEmpty()

    private companion object {
        val TITLE = Regex(
            pattern = "<title[^>]*>([^<]*)</title>",
            option = RegexOption.IGNORE_CASE
        )
    }
}

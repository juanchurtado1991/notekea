package app.notekea.clips.data

import app.notekea.clips.domain.Article
import app.notekea.clips.domain.ArticleExtractor
import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.FetchedPage
import app.notekea.core.domain.Outcome
import net.dankito.readability4j.Readability4J
import org.jsoup.Jsoup

/**
 * [ArticleExtractor] with Readability4J (Mozilla's Readability): the article's title and its paragraphs, headings and
 * list items as plain text; a page with too little text to be an article ([MIN_WORDS]) is not one.
 */
class ReadabilityArticleExtractor : ArticleExtractor {
    override fun extract(page: FetchedPage): Outcome<Article, ClipError> {
        val parsed = try {
            Readability4J(page.url, page.html).parse()
        } catch (exception: Exception) {
            return Outcome.Failure(error = ClipError.NotArticle)
        }

        val read = parsed
            .content
            ?.let { blocksOf(html = it) }
            .orEmpty()

        // A short documentation page had Readability take its menu for the article; the page's <main> holds it.
        val main = Jsoup
            .parse(page.html)
            .selectFirst(MAIN)
            ?.apply { select(CHROME).remove() }
            ?.let { blocksOf(html = it.html()) }
            .orEmpty()

        val isMainBetter =
            main.isNotEmpty() && proseOf(paragraphs = main) >= proseOf(paragraphs = read)

        val paragraphs = if (isMainBetter) main else read
        val words = paragraphs.sumOf { it.split(' ').size }
        val least = if (isMainBetter) MIN_MAIN_WORDS else MIN_WORDS
        if (words < least) return Outcome.Failure(error = ClipError.NotArticle)

        val title = parsed
            .title
            ?.trim()
            ?.takeIf { it.isNotEmpty() } ?: paragraphs.first().take(n = TITLE_CHARS)

        return Outcome.Success(
            value = Article(
                title = title,
                paragraphs = paragraphs
            )
        )
    }

    private fun blocksOf(html: String): List<String> = Jsoup
        .parse(html)
        .select(BLOCKS)
        .map { it.text().trim() }
        .filter { it.isNotEmpty() }
        .distinct()

    // The words in sentences, not in menu entries of a word or two.
    private fun proseOf(paragraphs: List<String>): Int = paragraphs
        .map { it.split(' ').size }
        .filter {
            it >= PROSE_WORDS
        }
        .sum()

    private companion object {
        // Mintlify's docs write paragraphs as <span data-as="p">.
        const val BLOCKS = "p, [data-as=p], h1, h2, h3, h4, li, blockquote, pre"
        const val MIN_WORDS = 80
        const val MIN_MAIN_WORDS = 30
        const val PROSE_WORDS = 6
        const val MAIN = "main, [role=main], #content, article"
        const val CHROME =
            "nav, aside, header, footer, [role=navigation], #sidebar, [id*=sidebar], [id*=navigation]"
        const val TITLE_CHARS = 60
    }
}

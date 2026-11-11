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

        val content = parsed.content ?: return Outcome.Failure(error = ClipError.NotArticle)

        val paragraphs = Jsoup
            .parse(content)
            .select(BLOCKS)
            .map { it.text().trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        val words = paragraphs.sumOf { it.split(' ').size }
        if (words < MIN_WORDS) return Outcome.Failure(error = ClipError.NotArticle)

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

    private companion object {
        const val BLOCKS = "p, h1, h2, h3, h4, li, blockquote, pre"
        const val MIN_WORDS = 80
        const val TITLE_CHARS = 60
    }
}

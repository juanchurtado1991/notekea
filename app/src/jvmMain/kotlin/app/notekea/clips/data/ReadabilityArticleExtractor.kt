package app.notekea.clips.data

import app.notekea.clips.domain.Article
import app.notekea.clips.domain.ArticleBlocks
import app.notekea.clips.domain.ArticleExtractor
import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.FetchedPage
import app.notekea.core.domain.Outcome
import net.dankito.readability4j.Readability4J
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

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
            ?.let {
                blocksOf(
                    root = Jsoup.parse(it, page.url).body(),
                    title = parsed.title
                )
            }
            .orEmpty()

        // A short documentation page had Readability take its menu for the article; the page's <main> holds it.
        val main = Jsoup
            .parse(page.html, page.url)
            .selectFirst(MAIN)
            ?.let {
                blocksOf(
                    root = it,
                    title = parsed.title
                )
            }
            .orEmpty()

        val isMainBetter =
            main.isNotEmpty() && proseOf(paragraphs = main) >= proseOf(paragraphs = read)

        val paragraphs = if (isMainBetter) main else read
        val words = paragraphs.sumOf { it.split(' ').size }
        val least = if (isMainBetter) MIN_MAIN_WORDS else MIN_WORDS
        if (words < least) return Outcome.Failure(error = ClipError.NotArticle)

        val title = parsed
            .title
            ?.let { ArticleBlocks.titleOf(title = it) }
            ?.takeIf { it.isNotEmpty() } ?: paragraphs.first().take(n = TITLE_CHARS)

        return Outcome.Success(
            value = Article(
                title = title,
                paragraphs = paragraphs
            )
        )
    }

    // The article as Markdown blocks, without what the page draws around it nor its title said again.
    private fun blocksOf(
        root: Element,
        title: String?
    ): List<String> {
        root.select(CHROME).remove()
        val named = title.orEmpty().trim()

        val blocks = HtmlMarkdown
            .blocksOf(root = root)
            .distinct()

        return ArticleBlocks
            .cleaned(blocks = blocks)
            .filterIndexed { index, block ->
                val words = block.trimStart('#', ' ')

                val isTitle =
                    index == 0 &&
                        block.startsWith(prefix = "#") &&
                        named.startsWith(prefix = words)

                !isTitle
            }
    }

    // The words in sentences, not in menu entries of a word or two.
    private fun proseOf(paragraphs: List<String>): Int = paragraphs
        .map { it.split(' ').size }
        .filter {
            it >= PROSE_WORDS
        }
        .sum()

    private companion object {
        const val MIN_WORDS = 80
        const val MIN_MAIN_WORDS = 30
        const val PROSE_WORDS = 6
        const val MAIN = "main, [role=main], #content, article"

        // Menus, the trail of where the page is, its table of contents, banners: around the article, not of it.
        const val CHROME =
            "nav, aside, footer, [role=navigation], [role=banner], [role=complementary], [role=search], " +
                "[role=alert], #sidebar, [id*=sidebar], [id*=navigation], [class*=breadcrumb], " +
                "[aria-label*=breadcrumb i], .toc, #toc, [class*=table-of-contents], [class*=tableofcontents], " +
                "[class*=on-this-page], [id*=in-this-article], [class*=in-this-article], [class*=skip-link], " +
                "[class*=cookie], [class*=feedback], [class*=edit-this-page], .sr-only, .visually-hidden, " +
                "main header nav, .mw-editsection, .navbox, .reflist, .noprint, .vector-header-container, " +
                ".vector-column-start, .vector-page-toolbar, .vector-dropdown, .vector-toc, .vector-menu, " +
                ".mw-portlet, .mw-jump-link, .catlinks, .printfooter, .mw-references-wrap, #p-lang-btn, " +
                "devsite-toc, devsite-feedback, devsite-bookmark, devsite-page-rating"
        const val TITLE_CHARS = 60
    }
}

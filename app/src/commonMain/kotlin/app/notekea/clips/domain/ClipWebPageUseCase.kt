package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A web page the user named, clipped (4.4): downloaded, its article taken out, notes written about it by the model and
 * kept only where the article backs them ([SupportCheck]). Without a model the article is clipped alone.
 */
class ClipWebPageUseCase(
    private val fetcher: PageFetcher,
    private val extractor: ArticleExtractor,
    private val llmEngine: LlmEngine,
    private val supportCheck: SupportCheck
) {
    operator fun invoke(url: String): Flow<ClipStep> = flow {
        emit(value = ClipStep.Fetching)

        // The page's Markdown copy first (a docs site's "page.md"): its text without menus; else the page itself.
        val copy = markdownOf(url = url.trim())

        val page = copy?.first ?: when (val fetched = fetcher.fetch(url = url.trim())) {
            is Outcome.Success -> fetched.value
            is Outcome.Failure -> return@flow emit(value = ClipStep.Failed(error = fetched.error))
        }

        val article = copy?.second ?: when (val extracted = extractedOf(page = page)) {
            is Outcome.Success -> extracted.value
            is Outcome.Failure -> return@flow emit(value = ClipStep.Failed(error = extracted.error))
        }

        val hasModel = llmEngine.readiness() is Outcome.Success

        if (!hasModel) {
            return@flow emit(
                value = ClipStep.Done(
                    clip = clipOf(
                        page = page,
                        article = article,
                        notes = ""
                    )
                )
            )
        }

        emit(value = ClipStep.Writing(title = article.title))
        val written = written(article = article)

        emit(value = ClipStep.Checking)

        val notes = written?.let {
            supportCheck.backed(
                notes = it,
                article = article
            )
        }.orEmpty()

        emit(
            value = ClipStep.Done(
                clip = clipOf(
                    page = page,
                    article = article,
                    notes = notes
                )
            )
        )
    }

    // A page served as Markdown (an llms.txt entry) is read as such; a web page goes to the extractor.
    private fun extractedOf(page: FetchedPage): Outcome<Article, ClipError> {
        if (!MarkdownArticle.isMarkdown(text = page.html)) return extractor.extract(page = page)

        return MarkdownArticle.of(text = page.html)?.let { Outcome.Success(value = it) }
            ?: Outcome.Failure(error = ClipError.NotArticle)
    }

    // "https://x.com/docs/a" read from "https://x.com/docs/a.md" when the site serves one; null when it does not.
    private suspend fun markdownOf(url: String): Pair<FetchedPage, Article>? {
        val isCopy = COPIED.containsMatchIn(input = url)
        val isFile = FILE.containsMatchIn(input = url.substringAfterLast(delimiter = '/'))

        val isOtherFile = isFile && !url.endsWith(suffix = ".html")
        if (isCopy || isOtherFile) return null

        val copy = url.trimEnd('/').removeSuffix(suffix = ".html") + ".md"
        val fetched = (fetcher.fetch(url = copy) as? Outcome.Success)?.value ?: return null
        if (!MarkdownArticle.isMarkdown(text = fetched.html)) return null

        val article = MarkdownArticle.of(text = fetched.html) ?: return null

        return fetched.copy(url = url) to article
    }

    // The model's notes, or none when it failed: the article is still worth keeping.
    private suspend fun written(article: Article): String? = try {
        val text = StringBuilder()

        llmEngine
            .generate(
                messages = ClipPrompt.messages(article = article),
                options = GenerationOptions(
                    maxTokens = MAX_TOKENS,
                    temperature = 0f
                )
            )
            .collect { text.append(it) }

        text
            .toString()
            .trim()
            .takeIf { it.isNotEmpty() }
    } catch (_: LlmException) {
        null
    }

    private fun clipOf(
        page: FetchedPage,
        article: Article,
        notes: String
    ) = WebClip(
        url = page.url,
        title = article.title,
        notes = notes,
        article = article
    )

    private companion object {
        const val MAX_TOKENS = 700
        val COPIED = Regex(
            pattern = "\\.(md|txt)$",
            option = RegexOption.IGNORE_CASE
        )
        val FILE = Regex(
            pattern = "\\.[a-z0-9]{2,5}$",
            option = RegexOption.IGNORE_CASE
        )
    }
}

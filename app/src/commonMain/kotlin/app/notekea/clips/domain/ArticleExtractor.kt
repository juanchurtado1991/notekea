package app.notekea.clips.domain

import app.notekea.core.domain.Outcome

/** Takes the article out of a page (Readability on desktop); [ClipError.NotArticle] when there is none. */
fun interface ArticleExtractor {
    fun extract(page: FetchedPage): Outcome<Article, ClipError>
}

package app.notekea.clips.di

import app.notekea.clips.data.DuckDuckGoSearch
import app.notekea.clips.data.KtorPageFetcher
import app.notekea.clips.data.ReadabilityArticleExtractor
import app.notekea.clips.domain.ArticleExtractor
import app.notekea.clips.domain.PageFetcher
import app.notekea.core.web.domain.WebSearchProvider
import org.koin.dsl.module

/** Web clips on the desktop: Ktor downloads the page, Readability4J takes the article out of it; and web search. */
val jvmClipsModule = module {
    single<PageFetcher> { KtorPageFetcher(dispatchers = get()) }
    single<ArticleExtractor> { ReadabilityArticleExtractor() }
    single<WebSearchProvider> { DuckDuckGoSearch(fetcher = get()) }
}

package app.notekea.clips.data

import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.FetchedPage
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What a clip may download and what it keeps of a page, with no network: addresses on this computer or its network,
 * and other schemes, are refused before any request; an article is taken out of its page, a home page is not one.
 */
class ClipAdaptersTest {
    private val fetcher = KtorPageFetcher(dispatchers = JvmAppDispatchers())
    private val extractor = ReadabilityArticleExtractor()

    @Test
    fun `local addresses and other schemes are never fetched`() = runBlocking {
        val refused = listOf(
            "http://localhost:8080/admin",
            "http://127.0.0.1/",
            "http://192.168.1.1/router",
            "http://10.0.0.5/",
            "http://[::1]/",
            "file:///etc/passwd",
            "ftp://example.com/file"
        ).map { (fetcher.fetch(url = it) as Outcome.Failure).error }

        assertEquals(
            expected = List(size = 5) { ClipError.LocalAddress } +
                List(size = 2) { ClipError.BadAddress },
            actual = refused
        )
    }

    @Test
    fun `an article keeps its title and paragraphs, without the menu, and a page with no article is told`() {
        val body = (1..6)
            .joinToString(separator = "") {
                "<p>Paragraph $it of the article explains how sleep sorts what was learned during the day, " +
                    "and why a short night makes memories weaker the next morning.</p>"
            }

        val page =
            "<html><head><title>Sleep and memory</title></head><body><nav><a href='/'>Home</a>" +
                "<a href='/about'>About</a></nav><article><h1>Sleep and memory</h1>$body</article>" +
                "<footer>© Example</footer></body></html>"

        val article = (
            extractor.extract(
                page = FetchedPage(
                    url = URL,
                    html = page
                )
            ) as Outcome.Success
            ).value

        val home = extractor.extract(
            page = FetchedPage(
                url = URL,
                html = "<html><body><a href='/'>Home</a></body></html>"
            )
        )

        assertEquals(
            expected = listOf<Any?>(
                "Sleep and memory",
                true,
                false,
                Outcome.Failure(error = ClipError.NotArticle)
            ),
            actual = listOf(
                article.title,
                article.paragraphs.any {
                    "Paragraph 6" in it
                },
                article.text.contains("About"),
                home
            )
        )
    }

    private companion object {
        const val URL = "https://example.com/sleep"
    }
}

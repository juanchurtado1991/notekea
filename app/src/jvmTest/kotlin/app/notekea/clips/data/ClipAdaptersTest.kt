package app.notekea.clips.data

import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.FetchedPage
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import kotlinx.coroutines.runBlocking
import java.net.InetAddress
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

    // Code review of 2026-11-11: an address checked public could turn local when looked up again to connect.
    // The Traceloop docs (2026-11-11): a short page had its menu taken for the article, and Mintlify writes paragraphs
    // as <span data-as="p">.
    @Test
    fun `a short docs page is read from its main, with its paragraphs written as spans, not its menu`() {
        val menu =
            (1..20).joinToString(separator = "") { "<li><a href=\"/docs/p$it\">Page $it</a></li>" }

        val code =
            "TRACELOOP_BASE_URL=https://api.axiom.co TRACELOOP_HEADERS=Authorization ".repeat(n = 8)

        val html =
            "<html><head><title>Axiom</title></head><body><main><div id=\"sidebar\"><ul>$menu</ul></div>" +
                "<div id=\"content\"><h1>LLM Observability with Axiom</h1><span data-as=\"p\">Axiom is an observability " +
                "platform that natively supports OpenTelemetry, you just need to route the traces to its endpoint.</span>" +
                "<pre>$code</pre></div></main></body></html>"

        val article = ReadabilityArticleExtractor().extract(
            page = FetchedPage(
                url = "https://x.com/docs/axiom",
                html = html
            )
        ) as Outcome.Success

        assertEquals(
            expected = listOf(true, false),
            actual = listOf(
                article
                    .value
                    .paragraphs
                    .any { it.startsWith(prefix = "Axiom is an observability platform") },
                article
                    .value
                    .paragraphs
                    .any {
                        it == "Page 1"
                    }
            )
        )
    }

    @Test
    fun `the lookup a request connects with refuses a host that turns local`() {
        var answers = 0

        val rebinding = LocalRefusingDns { host ->
            answers++
            val address = if (answers == 1) "93.184.216.34" else "127.0.0.1"
            listOf(InetAddress.getByAddress(host, InetAddress.getByName(address).address))
        }

        val first = rebinding.lookup(hostname = "example.com").map { it.hostAddress }
        val second = runCatching { rebinding.lookup(hostname = "example.com") }.exceptionOrNull()

        assertEquals(
            expected = listOf<Any?>(listOf("93.184.216.34"), true),
            actual = listOf(first, second is LocalRefusingDns.Refused)
        )
    }

    private companion object {
        const val URL = "https://example.com/sleep"
    }
}

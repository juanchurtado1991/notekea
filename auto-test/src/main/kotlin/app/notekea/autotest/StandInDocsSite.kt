package app.notekea.autotest

import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.FetchedPage
import app.notekea.clips.domain.PageFetcher
import app.notekea.core.domain.Outcome

/**
 * The web, in a case: a documentation site of [PAGES] pages at [ADDRESS] answered from memory, so clipping a section
 * needs no network (as sync's cases use a repository on disk); every other address goes to the [real] fetcher, which
 * refuses this computer's addresses (4.4-01).
 */
class StandInDocsSite(private val real: PageFetcher) : PageFetcher {
    override suspend fun fetch(url: String): Outcome<FetchedPage, ClipError> {
        if (!url.startsWith(prefix = ORIGIN)) return real.fetch(url = url)
        val name = url.removePrefix(prefix = "$ORIGIN/guide/")

        val html = when {
            name == INTRO -> pageOf(
                title = "Intro",
                links = (1..PAGES).joinToString(separator = "\n") { "<a href=\"/guide/page-$it\">Page $it</a>" }
            )

            PAGE.matches(input = name) -> pageOf(
                title = "Page ${name.removePrefix(prefix = "page-")}",
                links = ""
            )

            else -> return Outcome.Failure(error = ClipError.Refused(status = NOT_FOUND))
        }

        return Outcome.Success(
            value = FetchedPage(
                url = url,
                html = html
            )
        )
    }

    private fun pageOf(
        title: String,
        links: String
    ): String =
        "<html><head><title>$title</title></head><body><nav>$links</nav><main><h1>$title</h1>" +
            "<p>$title explains one part of the guide in sentences long enough to be read as an article. $PROSE</p>" +
            "<p>$PROSE</p></main></body></html>"

    companion object {
        const val PAGES = 34
        private const val ORIGIN = "https://docs.notekea.example"
        private const val INTRO = "intro"
        const val ADDRESS = "$ORIGIN/guide/$INTRO"
        private const val NOT_FOUND = 404
        private val PAGE = Regex(pattern = "page-\\d+")

        private const val PROSE =
            "Each step is written out with what it does and why it matters, so a reader " +
                "who follows it from the start finds the same result at the end, and a reader who skips ahead still " +
                "knows which earlier step to read again when something does not look as described here."
    }
}

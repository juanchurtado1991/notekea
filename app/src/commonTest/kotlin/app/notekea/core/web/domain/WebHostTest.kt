package app.notekea.core.web.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** The site of a web address, read one way by the search, an answer's links and a result. */
class WebHostTest {
    // The code review of 2026-11-22: two readers cut the host only at `/`, and one kept capitals.
    @Test
    fun `the site ends at the path, the query or the fragment, in lower case and without www`() {
        assertEquals(
            expected = listOf("fao.org", "fao.org", "fao.org", "fao.org", "fao.org", "evil.com"),
            actual = listOf(
                "https://www.fao.org/home/en",
                "https://FAO.org?lang=en",
                "https://WWW.fao.org#top",
                "https://fao.org:8443/data",
                "fao.org",
                "https://fao.org@evil.com/page"
            ).map { WebHost.of(address = it) }
        )
    }
}

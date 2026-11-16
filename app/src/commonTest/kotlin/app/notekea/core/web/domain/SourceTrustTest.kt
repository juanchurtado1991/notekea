package app.notekea.core.web.domain

import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemorySection
import kotlin.test.Test
import kotlin.test.assertEquals

/** Whether a page's site is trusted (`docs/agent-web.md`, "The pieces"). */
class SourceTrustTest {
    private fun line(text: String) = MemoryLine(
        section = MemorySection.SOURCES,
        text = text,
        origin = null
    )

    private val trust = SourceTrust(
        sources = listOf("www.lanacion.com.ar"),
        memory = listOf(line("Trusted: fao.org, *.edu"), line("Not trusted: blogdetomates.com"))
    )

    @Test
    fun `the vault's sources and the memory's trusted domains are trusted, by subdomain and suffix too`() {
        assertEquals(
            expected = List(size = 4) { SourceTrust.Trust.TRUSTED },
            actual = listOf("lanacion.com.ar", "www.fao.org", "news.fao.org", "mit.edu").map {
                trust.of(domain = it)
            }
        )
    }

    @Test
    fun `a domain the memory does not trust is not trusted, and one it does not name is new`() {
        assertEquals(
            expected = listOf(
                SourceTrust.Trust.NOT_TRUSTED,
                SourceTrust.Trust.NEW,
                SourceTrust.Trust.NEW
            ),
            actual = listOf("blogdetomates.com", "blogverde.com", "notfao.org").map {
                trust.of(domain = it)
            }
        )
    }
}

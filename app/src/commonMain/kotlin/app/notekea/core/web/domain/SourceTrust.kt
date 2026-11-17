package app.notekea.core.web.domain

import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemorySection

/**
 * Whether a page's site is trusted (`docs/agent-web.md`, "The pieces"): the vault's web sources and the domains the
 * memory trusts are, the ones it does not are not, and the rest are new, for the person to approve. By domain, as
 * people think of a source: "fao.org" covers its pages and subdomains, "*.edu" a suffix.
 */
class SourceTrust(
    sources: Collection<String>,
    memory: List<MemoryLine>
) {
    private val trusted: List<String>
    private val untrusted: List<String>

    init {
        val lines = memory.filter {
            it.section == MemorySection.SOURCES
        }

        val notTrusted = lines.filter { NOT.containsMatchIn(input = it.text) }

        untrusted = notTrusted.flatMap { domainsIn(text = it.text) }
        val own = sources.map { it.lowercase().removePrefix(prefix = "www.") }
        trusted = own + (lines - notTrusted.toSet()).flatMap { domainsIn(text = it.text) }
    }

    fun of(domain: String): Trust {
        val site = domain
            .lowercase()
            .removePrefix(prefix = "www.")

        // Only a bare host is judged: one with a query, a path or a user in it ("evil.com?x=.fao.org") could end in a
        // trusted suffix it is not on.
        val isHost = HOST.matches(input = site)

        return when {
            !isHost -> Trust.NEW

            untrusted.any {
                covers(
                    pattern = it,
                    site = site
                )
            } -> Trust.NOT_TRUSTED

            trusted.any {
                covers(
                    pattern = it,
                    site = site
                )
            } -> Trust.TRUSTED

            else -> Trust.NEW
        }
    }

    private fun covers(
        pattern: String,
        site: String
    ): Boolean = if (pattern.startsWith(prefix = "*.")) {
        site.endsWith(suffix = pattern.drop(n = 1))
    } else {
        site == pattern || site.endsWith(suffix = ".$pattern")
    }

    private fun domainsIn(text: String): List<String> = DOMAIN
        .findAll(input = text)
        .map {
            it
                .value
                .lowercase()
                .removePrefix(prefix = "www.")
        }
        .toList()

    enum class Trust { TRUSTED, NOT_TRUSTED, NEW }

    private companion object {
        val DOMAIN = Regex(
            pattern = "\\*\\.[a-z]{2,}|[a-z0-9][a-z0-9-]*(\\.[a-z0-9-]+)*\\.[a-z]{2,}",
            option = RegexOption.IGNORE_CASE
        )

        // Letters of any script, for a site written in its own ("café.com").
        val HOST = Regex(pattern = "[\\p{L}\\p{N}-]+(\\.[\\p{L}\\p{N}-]+)*")

        val NOT = Regex(
            pattern = "\\bnot trusted\\b|\\bno confiable\\b|\\bno conf[ií]o\\b|\\bnever\\b|\\bnunca\\b",
            option = RegexOption.IGNORE_CASE
        )
    }
}

package app.notekea.chat.domain.tool

import app.notekea.core.web.domain.FoundSource
import app.notekea.core.web.domain.SourceTrust

/**
 * What a turn found on the web (`docs/agent-web.md`, #144): the pages it may read ([readable], of trusted sites or
 * approved by the person) and the sources waiting for the person to approve ([found]).
 */
class TurnWeb {
    private val readableUrls = mutableSetOf<String>()
    private val waiting = mutableListOf<FoundSource>()

    val readable: Set<String> get() = readableUrls.toSet()

    /** Every source found that is not left out as not trusted: the trusted ones and the new ones. */
    val found: List<FoundSource> get() = waiting.filter {
        it.trust != SourceTrust.Trust.NOT_TRUSTED
    }

    /** Whether a source waits for the person: one found that is neither trusted nor left out. */
    val isWaiting: Boolean get() = waiting.any {
        it.trust == SourceTrust.Trust.NEW
    }

    /** How many sources were left out as not trusted, said under the list. */
    val omitted: Int get() = waiting.count {
        it.trust == SourceTrust.Trust.NOT_TRUSTED
    }

    // The searches and the pages read this turn: three and eight at most (#144).
    private val searches = mutableListOf<String>()
    private val pages = mutableListOf<String>()

    val canSearch: Boolean get() = searches.size < MOST_SEARCHES
    val canRead: Boolean get() = pages.size < MOST_PAGES

    fun add(sources: List<FoundSource>) {
        waiting += sources
    }

    fun searched(query: String) {
        searches += query
    }

    fun read(url: String) {
        pages += url
    }

    fun allow(url: String) {
        readableUrls += url
    }

    private companion object {
        const val MOST_SEARCHES = 3
        const val MOST_PAGES = 8
    }
}

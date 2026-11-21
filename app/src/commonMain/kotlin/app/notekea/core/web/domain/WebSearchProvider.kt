package app.notekea.core.web.domain

import app.notekea.core.domain.Outcome

/**
 * A web search the chat may make (`docs/agent-web.md`, `GUIDELINES.md` §13 and #144): the [query] as it was shown to
 * the person, and at most the [country] (an ISO code, "CO") besides, never the notes' text.
 */
fun interface WebSearchProvider {
    suspend fun search(
        query: String,
        country: String?
    ): Outcome<List<WebResult>, WebSearchProblem>
}

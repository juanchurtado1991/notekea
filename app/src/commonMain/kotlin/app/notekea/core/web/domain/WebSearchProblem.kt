package app.notekea.core.web.domain

/** Why a web search gave nothing: offline, blocked or refused by the provider, or no results. */
enum class WebSearchProblem {
    OFFLINE,
    BLOCKED,
    NOTHING_FOUND
}

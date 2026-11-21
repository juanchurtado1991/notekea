package app.notekea.core.web.domain

/** A page a web search found, with whether its site is trusted ([trust]), for the person to approve when it is new. */
data class FoundSource(
    val result: WebResult,
    val trust: SourceTrust.Trust
)

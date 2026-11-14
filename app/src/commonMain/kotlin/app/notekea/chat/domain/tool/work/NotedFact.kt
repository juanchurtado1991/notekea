package app.notekea.chat.domain.tool.work

/** Something a turn found and kept: the [fact], and the [passage] of what it read that says so. */
data class NotedFact(
    val fact: String,
    val passage: String
)

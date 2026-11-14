package app.notekea.chat.domain.sources

/**
 * A page of a site the vault has web sources of, not clipped yet, that may hold what the notes lack: its [url],
 * what it is ([about]) and the note of the section it would join ([section], a path in the vault).
 */
data class SourcePage(
    val url: String,
    val about: String,
    val section: String
)

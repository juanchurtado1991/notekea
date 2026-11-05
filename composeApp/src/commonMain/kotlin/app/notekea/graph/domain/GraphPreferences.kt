package app.notekea.graph.domain

/** What the graph keeps for a vault: the top-level [excludedFolders] it leaves out (`ROADMAP.md` 2.7). */
data class GraphPreferences(
    val excludedFolders: Set<String> = emptySet()
)

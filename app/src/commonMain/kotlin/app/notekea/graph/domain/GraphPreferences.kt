package app.notekea.graph.domain

/** What the graph keeps for a vault: the top-level [excludedFolders] it leaves out. */
data class GraphPreferences(val excludedFolders: Set<String> = emptySet())

package app.notekea.notes.domain

/**
 * What the inspector's Connections section shows (`ROADMAP.md` 2.4, 2.5): the notes related by meaning, the notes that
 * link to the open one, or those that name it without a link. The choice is remembered like the folded sections.
 */
enum class ConnectionsTab {
    RELATED,
    LINKED,
    UNLINKED
}

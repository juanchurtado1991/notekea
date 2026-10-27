package app.notekea.notes.domain

/**
 * What the panel on the right of the editor shows: the inspector of the open note (its sections) or the chat with the
 * notes (`ROADMAP.md` 3.3), which says it is coming until it is built.
 */
enum class SidePanelMode {
    INSPECTOR,
    CHAT
}

package app.notekea.notes.domain

/**
 * One pane of the editor (`ROADMAP.md` 1.11): its open [tabs], each a note or another file of the vault, in the order
 * they are shown, and the index of the [active] one, or -1 when the pane has no tab.
 */
data class EditorPane(
    val tabs: List<RelativePath> = emptyList(),
    val active: Int = NONE
) {
    val activePath: RelativePath?
        get() = tabs.getOrNull(index = active)

    companion object {
        const val NONE = -1
    }
}

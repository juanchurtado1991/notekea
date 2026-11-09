package app.notekea.core.notes.domain

/**
 * How the user left the panel on the right of the editor, the same for every vault: how wide it is, between
 * [MIN_WIDTH_DP] and [MAX_WIDTH_DP], whether it is shown, its [mode], which of its sections are folded
 * ([foldedSections]) and what Connections shows ([connections]).
 */
data class SidePanelPreferences(
    val widthDp: Int = DEFAULT_WIDTH_DP,
    val isVisible: Boolean = true,
    val mode: SidePanelMode = SidePanelMode.INSPECTOR,
    val foldedSections: Set<SidePanelSection> = emptySet(),
    val connections: ConnectionsTab = ConnectionsTab.RELATED
) {
    companion object {
        const val DEFAULT_WIDTH_DP = 340
        const val MAX_WIDTH_DP = 560
        const val MIN_WIDTH_DP = 280
        val DEFAULT = SidePanelPreferences()

        fun clampWidth(widthDp: Int): Int = widthDp.coerceIn(
            minimumValue = MIN_WIDTH_DP,
            maximumValue = MAX_WIDTH_DP
        )
    }
}

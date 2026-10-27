package app.notekea.notes.domain

/**
 * How the user left the panel on the right of the editor (`ROADMAP.md` 1.9): how wide it is, between
 * [MIN_WIDTH_DP] and [MAX_WIDTH_DP], whether it is shown, its [mode], and which of its sections are folded
 * ([foldedSections]).
 * The same for every vault.
 */
data class SidePanelPreferences(
    val widthDp: Int = DEFAULT_WIDTH_DP,
    val isVisible: Boolean = true,
    val mode: SidePanelMode = SidePanelMode.INSPECTOR,
    val foldedSections: Set<SidePanelSection> = emptySet()
) {
    companion object {
        const val DEFAULT_WIDTH_DP = 340
        const val MAX_WIDTH_DP = 560
        const val MIN_WIDTH_DP = 280
        val DEFAULT = SidePanelPreferences()

        fun clampWidth(
            widthDp: Int
        ): Int = widthDp.coerceIn(minimumValue = MIN_WIDTH_DP, maximumValue = MAX_WIDTH_DP)
    }
}

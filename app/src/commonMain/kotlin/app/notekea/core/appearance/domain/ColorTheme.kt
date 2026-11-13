package app.notekea.core.appearance.domain

/**
 * The app's color themes: [WORKSPACE_BLUE], the one the app starts with, and six 60/30/10 palettes, where the page
 * takes the first color, the sidebar and the tabs the second, and what can be clicked the third. [id] is how the
 * choice is kept between runs, so renaming a theme never loses it.
 */
enum class ColorTheme(val id: String) {
    WORKSPACE_BLUE(id = "workspace_blue"),
    POLAR(id = "polar"),
    DESERT(id = "desert"),
    STORM(id = "storm"),
    FOREST(id = "forest"),
    OCEAN(id = "ocean"),
    LAVENDER(id = "lavender");

    companion object {
        /** The theme of those who have not chosen one. */
        val DEFAULT = WORKSPACE_BLUE

        // Themes that are gone, each kept by the closest one, so nobody chose a theme to lose it.
        private val RETIRED = mapOf(
            "default" to WORKSPACE_BLUE,
            "azure_cloud" to WORKSPACE_BLUE,
            "rose" to POLAR,
            "signal_almond" to POLAR,
            "graphite" to STORM,
            "clay" to DESERT,
            "honey" to DESERT,
            "clay_butter" to DESERT,
            "neon_porcelain" to DESERT,
            "sage" to FOREST,
            "midnight_lime" to FOREST,
            "deep_sage" to FOREST,
            "forest_mint" to FOREST,
            "prompt_green" to FOREST,
            "lagoon" to OCEAN,
            "iris" to LAVENDER,
            "cosmic_violet" to LAVENDER
        )

        /** The theme kept as [id], the closest one if it is gone, or [DEFAULT] when there is none or it is unknown. */
        fun fromId(id: String?): ColorTheme = entries.firstOrNull {
            it.id == id
        } ?: RETIRED[id] ?: DEFAULT
    }
}

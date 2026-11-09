package app.notekea.core.appearance.domain

/**
 * The app's color themes: [WORKSPACE_BLUE], the one the app starts with, and seven in its image, gray surfaces with a
 * touch of hue and one lead color. [id] is how the choice is kept between runs, so renaming a theme never loses it.
 */
enum class ColorTheme(val id: String) {
    WORKSPACE_BLUE(id = "workspace_blue"),
    GRAPHITE(id = "graphite"),
    SAGE(id = "sage"),
    LAGOON(id = "lagoon"),
    IRIS(id = "iris"),
    ROSE(id = "rose"),
    CLAY(id = "clay"),
    HONEY(id = "honey");

    companion object {
        /** The theme of those who have not chosen one. */
        val DEFAULT = WORKSPACE_BLUE

        // Themes that are gone, each kept by the closest one, so nobody chose a theme to lose it.
        private val RETIRED = mapOf(
            "default" to WORKSPACE_BLUE,
            "azure_cloud" to WORKSPACE_BLUE,
            "midnight_lime" to SAGE,
            "deep_sage" to SAGE,
            "forest_mint" to SAGE,
            "prompt_green" to LAGOON,
            "cosmic_violet" to IRIS,
            "signal_almond" to ROSE,
            "neon_porcelain" to CLAY,
            "clay_butter" to HONEY
        )

        /** The theme kept as [id], the closest one if it is gone, or [DEFAULT] when there is none or it is unknown. */
        fun fromId(id: String?): ColorTheme = entries.firstOrNull {
            it.id == id
        } ?: RETIRED[id] ?: DEFAULT
    }
}

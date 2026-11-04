package app.notekea.core.appearance.domain

/**
 * The app's color themes (`ROADMAP.md` 2.12): [WORKSPACE_BLUE], the one the app starts with, [COBALT_TANGERINE], its
 * first palette and so still kept as `default`, and nine made from a deep and a light tone. [id] is how the choice is
 * kept between runs, so renaming a theme never loses it.
 */
enum class ColorTheme(val id: String) {
    WORKSPACE_BLUE(id = "workspace_blue"),
    COBALT_TANGERINE(id = "default"),
    MIDNIGHT_LIME(id = "midnight_lime"),
    DEEP_SAGE(id = "deep_sage"),
    SIGNAL_ALMOND(id = "signal_almond"),
    COSMIC_VIOLET(id = "cosmic_violet"),
    AZURE_CLOUD(id = "azure_cloud"),
    NEON_PORCELAIN(id = "neon_porcelain"),
    FOREST_MINT(id = "forest_mint"),
    PROMPT_GREEN(id = "prompt_green"),
    CLAY_BUTTER(id = "clay_butter");

    companion object {
        /** The theme of those who have not chosen one. */
        val DEFAULT = WORKSPACE_BLUE

        /** The theme kept as [id], or [DEFAULT] when there is none or it is unknown (a later version's theme). */
        fun fromId(id: String?): ColorTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

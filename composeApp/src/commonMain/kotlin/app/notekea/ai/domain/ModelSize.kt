package app.notekea.ai.domain

/** The three chat model sizes of `ROADMAP.md` 3.1, by the [id] kept in the settings, smallest first. */
enum class ModelSize(
    val id: String
) {
    SMALL(id = "small"),
    MEDIUM(id = "medium"),
    LARGE(id = "large");

    companion object {
        /** The size kept as [id], or null when nothing, or a size that no longer exists, was kept. */
        fun fromId(
            id: String?
        ): ModelSize? = entries.firstOrNull { it.id == id }
    }
}

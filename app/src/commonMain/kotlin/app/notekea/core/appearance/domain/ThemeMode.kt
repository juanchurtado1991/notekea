package app.notekea.core.appearance.domain

/** Whether the app is light or dark: as the operating system is ([SYSTEM]), or always one of them. */
enum class ThemeMode(val id: String) {
    SYSTEM(id = "system"),
    LIGHT(id = "light"),
    DARK(id = "dark");

    companion object {
        /** The mode kept as [id], or [SYSTEM] when there is none or it is unknown. */
        fun fromId(id: String?): ThemeMode = entries.firstOrNull {
            it.id == id
        } ?: SYSTEM
    }
}

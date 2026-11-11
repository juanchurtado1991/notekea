package app.notekea.core.preferences.domain

/** The language of Notekea's own words, by its [id]: the computer's ([SYSTEM]), English or Spanish (5.2). */
enum class AppLanguage(val id: String) {
    SYSTEM(id = "system"),
    ENGLISH(id = "en"),
    SPANISH(id = "es");

    companion object {
        fun fromId(id: String?): AppLanguage = entries.firstOrNull {
            it.id == id
        } ?: SYSTEM
    }
}

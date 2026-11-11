package app.notekea.core.preferences.domain

/** How wide a note's lines go, by its [id]: a column easy to read, the wide page of before, or the whole pane. */
enum class LineWidth(val id: String) {
    READABLE(id = "readable"),
    WIDE(id = "wide"),
    FULL(id = "full");

    companion object {
        fun fromId(id: String?): LineWidth = entries.firstOrNull {
            it.id == id
        } ?: WIDE
    }
}

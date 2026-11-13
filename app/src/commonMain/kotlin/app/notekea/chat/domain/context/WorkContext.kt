package app.notekea.chat.domain.context

/** Where the person is in Notekea: a note, another file, a folder, one of the views, or Settings. */
enum class WorkPlace(val said: String) {
    NOTE(said = "the note"),
    FILE(said = "the file"),
    FOLDER(said = "the folder"),
    GRAPH(said = "the graph view"),
    QUERY(said = "the query view"),
    TASKS(said = "the tasks view"),
    STUDY(said = "the study cards"),
    SETTINGS(said = "Settings")
}

/**
 * What the person has open as they ask (the developer's choice of 2026-11-13: always, shown as a chip): the [place]
 * and its [name] (a note's or folder's path), the [section] the cursor is in and the text [selected], if any.
 */
data class WorkContext(
    val place: WorkPlace,
    val name: String = "",
    val section: String? = null,
    val selected: String? = null
) {
    /** The context as the model reads it, one line: "The person is in the note Recipes/Arepas, at Ingredients." */
    fun described(): String {
        val where = listOf(place.said, name)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")

        val at = section?.let { ", at the section \"$it\"" }.orEmpty()

        val chosen = selected
            ?.take(n = MAX_SELECTED)
            ?.let { " They selected this text: \"$it\"." }
            .orEmpty()

        return "The person is in $where$at.$chosen"
    }

    companion object {
        /** The most of a selection the model reads, and the chip shows a short start of it. */
        const val MAX_SELECTED = 1_500
    }
}

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
    YOUTUBE(said = "a YouTube video"),
    SETTINGS(said = "Settings")
}

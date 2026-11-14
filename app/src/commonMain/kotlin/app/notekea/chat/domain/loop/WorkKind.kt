package app.notekea.chat.domain.loop

/**
 * The kinds of work a turn can be, each with its own few tools (`docs/agent-loop.md`, "Toolsets"): the router names
 * one from the request, as the model reads it ([written], [description]).
 */
enum class WorkKind(
    val written: String,
    val description: String
) {
    WRITE(
        written = "write",
        description = "a new note is written (a summary, a table, a comparison, a list, study cards), " +
            "whatever it is written from (notes, tasks, meetings), an empty note is created, or text is added."
    ),
    ORGANIZE(
        written = "organize",
        description = "notes are tagged, given a property or a task, linked, moved, renamed, bookmarked or " +
            "trashed, or a folder is created; a task is marked done."
    ),
    WEB(
        written = "web",
        description = "a web page whose address the request writes is read or saved."
    ),
    ASK(
        written = "ask",
        description = "only when the request changes nothing: a question answered from the notes, or " +
            "something counted, compared or looked up in them."
    )
}

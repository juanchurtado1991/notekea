package app.notekea.chat.domain.loop

/**
 * The kinds of work a turn can be, each with its own few tools (`docs/agent-loop.md`, "Toolsets"): the router names
 * one from the request, as the model reads it ([written], [description]).
 */
enum class WorkKind(
    val written: String,
    val description: String,
    val isReply: Boolean = false
) {
    WRITE(
        written = "write",
        description = "a new note is written (a summary, a table, a comparison, a list, study cards or a " +
            "quiz), whatever it is written from (notes, tasks, meetings), an empty note is created, or text " +
            "is added to a note. Never a task, tag, property or link added to a note (organize)."
    ),
    ORGANIZE(
        written = "organize",
        description = "notes are tagged, given a property or a task (add a task to…), linked, moved, renamed, " +
            "bookmarked or trashed, or a folder is created; a task is marked done; the chat is asked to remember " +
            "or forget something from now on (remember…, recuerda…, forget…, olvida…)."
    ),
    WEB(
        written = "web",
        description = "the web: searched for something the notes do not hold (search the web, look it up online, " +
            "busca en internet, where can I buy…), or a page whose address the request writes read or saved."
    ),
    ASK(
        written = "ask",
        description = "only when the request changes nothing: a question answered from the notes, or " +
            "something counted, compared or looked up in them, or notes and tasks found and listed."
    ),
    APP(
        written = "app",
        description = "the app itself does something on screen now: a note or today's note opened, a panel " +
            "(backlinks, outline, graph, bookmarks…), the app's search opened on words, the theme, the panes, " +
            "the note as Markdown. Never notes or tasks listed (ask), a bookmark, tag or property (organize), " +
            "or how to do it (help)."
    ),

    // Replies the loop hands to the chat's own answers, with no tool to call (`docs/agent-loop.md`, step 6).
    TALK(
        written = "talk",
        description = "not about anything the notes hold: a greeting, thanks, small talk, or the chat's last " +
            "reply asked again another way (shorter, in Spanish).",
        isReply = true
    ),
    HELP(
        written = "help",
        description = "how to use Notekea itself, or what the chat can do: where a feature is, a shortcut, how " +
            "do I do something in the app (a question, never done for them).",
        isReply = true
    ),
    OVERVIEW(
        written = "overview",
        description = "what the vault holds as a whole: its folders, its topics, what is in it.",
        isReply = true
    )
}

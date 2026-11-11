package app.notekea.chat.presentation

/** Where a question of the chat stands, as one line under it says in plain words. */
enum class ChatStep {
    /** The model chooses whether to answer or what to do. */
    PLANNING,
    SEARCHING,

    /** The first search found nothing: the agent searches with other words or reads whole notes. */
    LOOKING_FURTHER,
    READING,
    WRITING,

    /** Deep answer: drafts are written and compared, so nothing shows before the chosen one. */
    DRAFTING,

    /** The text of the notes an action will write is being written. */
    COMPOSING,

    /** Agent mode works on a task, move after move (4.7). */
    WORKING,
    DONE
}

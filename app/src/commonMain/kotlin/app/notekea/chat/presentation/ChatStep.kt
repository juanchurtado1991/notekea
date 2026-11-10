package app.notekea.chat.presentation

/** Where a question of the chat stands, as one line under it says in plain words. */
enum class ChatStep {
    /** The model chooses whether to answer or what to do. */
    PLANNING,
    SEARCHING,
    READING,
    WRITING,

    /** Deep answer: drafts are written and compared, so nothing shows before the chosen one. */
    DRAFTING,

    /** The text of the notes an action will write is being written. */
    COMPOSING,
    DONE
}

package app.notekea.chat.domain.loop

/** What an entry of a note is: a row of a table is never what a task or an item of a list could be meant for. */
internal enum class NoteEntryKind {
    ROW,
    TASK,
    ITEM
}

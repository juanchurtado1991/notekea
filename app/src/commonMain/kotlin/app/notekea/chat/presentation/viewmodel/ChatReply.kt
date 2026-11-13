package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.core.notes.domain.RelativePath

/** What is left to do once a request is planned: nothing more, answer it, talk, explain the app or the vault. */
internal sealed interface ChatReply {
    data object Acted : ChatReply

    /** Answer from the notes, searching for [question], the request as it stands alone. */
    data class Question(val question: String) : ChatReply

    /** Talk (a greeting, an opinion), reading the [note] it is about, if any. */
    data class Talk(val note: RelativePath?) : ChatReply

    /** How to use Notekea, [request] being what they want to know as it stands alone. */
    data class Help(val request: String) : ChatReply

    /** What the vault holds as a whole. */
    data object Overview : ChatReply

    /** A question the plan answered with [tables] of notes: answered in words from them, above them. */
    data class FromFound(
        val question: String,
        val tables: List<FoundNotes>
    ) : ChatReply

    /** Agent mode works on it (4.7): its moves are listed on the entry, its end is a card or what it found. */
    data object Agent : ChatReply
}

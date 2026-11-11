package app.notekea.chat.presentation

import app.notekea.core.notes.domain.RelativePath

/** What is left to do once a request is planned: nothing more, answer it from the notes, or talk. */
internal sealed interface ChatReply {
    data object Acted : ChatReply

    /** Answer from the notes, searching for [question], the request as it stands alone. */
    data class Question(val question: String) : ChatReply

    /** Talk (a greeting, an opinion), reading the [note] it is about, if any. */
    data class Talk(val note: RelativePath?) : ChatReply
}

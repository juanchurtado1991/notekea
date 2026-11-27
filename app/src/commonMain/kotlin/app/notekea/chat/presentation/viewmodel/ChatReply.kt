package app.notekea.chat.presentation.viewmodel

import app.notekea.core.notes.domain.RelativePath

/** The chat's own replies the loop hands back: talk, how to use the app, what the vault holds. */
internal sealed interface ChatReply {
    /** Talk (a greeting, an opinion), reading the [note] it is about, if any. */
    data class Talk(val note: RelativePath?) : ChatReply

    /** How to use Notekea, [request] being what they want to know as it stands alone. */
    data class Help(val request: String) : ChatReply

    /** What the vault holds as a whole. */
    data object Overview : ChatReply
}

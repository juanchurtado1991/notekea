package app.notekea.core.logging.domain

/** The system clipboard, for a text a person pastes elsewhere (a report, a message). */
interface TextClipboard {
    fun copy(
        text: String
    )
}

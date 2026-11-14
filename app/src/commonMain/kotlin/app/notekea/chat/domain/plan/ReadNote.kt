package app.notekea.chat.domain.plan

/**
 * A note as the chat reads it to work from: its [text] with the text of the documents it attaches, the [attached]
 * documents read, and the files [unread] it attaches that hold no text Notekea can read (a scanned PDF, a picture).
 */
data class ReadNote(
    val text: String,
    val attached: List<String> = emptyList(),
    val unread: List<String> = emptyList()
)

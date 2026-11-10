package app.notekea.chat.domain

/**
 * An answer from the notes: its [text], which cites its [sources] as `[1]`, `[2]`… in the order they first appear,
 * and every passage that was read to write it ([read]), cited or not.
 */
data class Answer(
    val text: String,
    val sources: List<AnswerSource>,
    val read: List<AnswerSource>
)

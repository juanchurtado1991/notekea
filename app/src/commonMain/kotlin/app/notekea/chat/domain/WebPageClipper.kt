package app.notekea.chat.domain

import app.notekea.core.domain.Outcome

/** Clips the web page at a URL into the note it becomes (4.4), for the chat; the shell gives it the clips feature. */
fun interface WebPageClipper {
    suspend fun clip(url: String): Outcome<NoteDraft, PageProblem>
}

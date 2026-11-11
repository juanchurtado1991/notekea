package app.notekea.chat.domain

import app.notekea.core.domain.Outcome

/** Reads the article of a web page the task wrote, for agent mode (4.7); the shell gives it the clips feature. */
fun interface PageReader {
    suspend fun read(url: String): Outcome<String, PageProblem>
}

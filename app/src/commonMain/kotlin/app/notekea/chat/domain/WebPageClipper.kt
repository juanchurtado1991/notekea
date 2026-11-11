package app.notekea.chat.domain

import app.notekea.core.domain.Outcome

/**
 * Clips the web page at a URL into the note it becomes (4.4), for the chat, or with the pages it links under its own
 * path into a folder of notes; the shell gives it the clips feature.
 */
interface WebPageClipper {
    suspend fun clip(url: String): Outcome<NoteDraft, PageProblem>

    /** How many pages the page links under its path ([SectionAsked.prefix]); none when it links none. */
    suspend fun sectionOf(url: String): Outcome<SectionAsked, PageProblem>

    /** The page and the pages it links under its path, each a note, the page their index. */
    suspend fun clipSection(url: String): Outcome<List<NoteDraft>, PageProblem>
}

/** The pages a page links under its own [prefix]: [count] of them, [more] left out over the limit. */
data class SectionAsked(
    val url: String,
    val prefix: String,
    val count: Int,
    val more: Int
)

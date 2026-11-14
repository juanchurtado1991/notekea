package app.notekea.chat.domain.plan

import app.notekea.core.domain.Outcome

/**
 * Clips the web page at a URL into the note it becomes (4.4), for the chat, or with the pages it links under its own
 * path into a folder of notes; the shell gives it the clips feature.
 */
interface WebPageClipper {
    suspend fun clip(url: String): Outcome<NoteDraft, PageProblem>

    /** How many pages the page links under its path ([SectionAsked.prefix]); none when it links none. */
    suspend fun sectionOf(
        url: String,
        language: String? = null
    ): Outcome<SectionAsked, PageProblem>

    /**
     * The pages [depth] levels of links give, counted before any is clipped ([SectionAsked.isCounted]), [onProgress]
     * told how many so far.
     */
    suspend fun counted(
        url: String,
        depth: Int,
        onProgress: suspend (PageProgress) -> Unit = {}
    ): Outcome<SectionAsked, PageProblem>

    /**
     * The page and the pages it links under its path, each a note, the page their index; [depth] levels of links
     * followed, [onProgress] told how far it is.
     */
    suspend fun clipSection(
        url: String,
        isEvery: Boolean = false,
        depth: Int = 1,
        onProgress: suspend (PageProgress) -> Unit = {}
    ): Outcome<List<NoteDraft>, PageProblem>
}

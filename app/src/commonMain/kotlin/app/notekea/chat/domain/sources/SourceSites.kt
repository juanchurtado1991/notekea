package app.notekea.chat.domain.sources

import app.notekea.chat.domain.plan.PageProgress

/**
 * The sites the vault has web sources of, for a question its notes do not answer (the developer's request of
 * 2026-11-13): which they are, their pages not clipped yet closest to the question, and those clipped into their
 * sections. The shell gives it the clips feature; it never looks outside the sites already clipped from.
 */
interface SourceSites {
    suspend fun sitesOf(vaultPath: String): List<String>

    suspend fun pagesFor(
        vaultPath: String,
        question: String,
        onProgress: suspend (PageProgress) -> Unit
    ): List<SourcePage>

    suspend fun clip(
        vaultPath: String,
        pages: List<SourcePage>,
        onProgress: suspend (PageProgress) -> Unit
    ): List<ClippedPage>
}

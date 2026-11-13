package app.notekea.search.domain

import app.notekea.core.notes.domain.index.IndexRunError

/**
 * How an indexing run goes: [done] of the vault's [total] notes (those indexed before the run count as done, so a
 * resumed run goes on from where the last one stopped), how many it [indexed] again, the notes it [skipped] and why,
 * and, once it ends, whether it [isFinished] or stopped on an [error].
 */
data class IndexProgress(
    val total: Int,
    val done: Int = 0,
    val indexed: Int = 0,
    val skipped: List<SkippedNote> = emptyList(),
    val isFinished: Boolean = false,
    val error: IndexRunError? = null
)

package app.notekea.notes.domain

/**
 * How an indexing run goes (`ROADMAP.md` 2.3): [done] of the vault's [total] notes (the ones indexed before the run
 * count as done, so a resumed run goes on from where the last one stopped), how many it [indexed] again (the others'
 * text had not changed), the notes it [skipped] and why, and, once it ends, whether it [isFinished] or stopped
 * on an [error] (the rest waits for the next run).
 */
data class IndexProgress(
    val total: Int,
    val done: Int = 0,
    val indexed: Int = 0,
    val skipped: List<SkippedNote> = emptyList(),
    val isFinished: Boolean = false,
    val error: IndexRunError? = null
)

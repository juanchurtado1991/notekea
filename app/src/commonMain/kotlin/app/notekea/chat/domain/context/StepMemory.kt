package app.notekea.chat.domain.context

/**
 * What a turn's steps found, kept outside the model's context and handed back in parts by what the task needs
 * (`docs/agent-step-memory.md`): a result the context no longer holds is not lost, its parts that match come back.
 */
interface StepMemory {
    /** Keeps the [chunks] of the [entry]th result of the turn [space]. */
    suspend fun keep(
        space: String,
        entry: Int,
        chunks: List<String>
    )

    /** The parts of [space]'s results among [entries] that match [need], the best first, [most] at most. */
    suspend fun ranked(
        space: String,
        need: String,
        entries: Set<Int>,
        most: Int
    ): List<StepChunk>

    /** Forgets everything [space] kept, at the turn's end. */
    suspend fun forget(space: String)
}

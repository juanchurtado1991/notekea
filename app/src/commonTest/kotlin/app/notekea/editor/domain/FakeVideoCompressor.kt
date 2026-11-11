package app.notekea.editor.domain

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * [VideoCompressor] in memory: files over the limit are those in [sizes]; compressing gives [steps], or waits until it
 * is cancelled when [isStuck], and the copies discarded are kept in [discarded].
 */
class FakeVideoCompressor(
    val sizes: MutableMap<String, Long> = mutableMapOf(),
    var steps: List<CompressStep> = emptyList(),
    var isStuck: Boolean = false
) : VideoCompressor {
    val discarded = mutableListOf<String>()

    override suspend fun isOverLimit(
        source: String,
        maxBytes: Long
    ): Boolean = (sizes[source] ?: 0L) > maxBytes

    override fun compress(
        source: String,
        maxBytes: Long
    ): Flow<CompressStep> = flow {
        steps.forEach { emit(value = it) }
        if (isStuck) awaitCancellation()
    }

    override suspend fun discard(path: String) {
        discarded += path
    }
}

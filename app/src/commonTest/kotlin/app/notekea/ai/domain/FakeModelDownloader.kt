package app.notekea.ai.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * [ModelDownloader] that writes into a [FakeModelStorage] in [chunks] steps: a file in [failing] stops halfway with
 * a network error, and a file in [held] waits after its first half, as a slow connection does, until it
 * is cancelled (paused). Every call is kept in [calls], with the byte it resumed from.
 */
class FakeModelDownloader(
    private val storage: FakeModelStorage,
    private val chunks: Int = 2
) : ModelDownloader {
    val calls = mutableListOf<Pair<String, Long>>()
    val failing = mutableSetOf<String>()
    val held = mutableSetOf<String>()

    override fun download(
        file: ModelFile,
        fromByte: Long
    ): Flow<DownloadStep> = flow {
        calls += file.fileName to fromByte
        val step = (file.sizeBytes - fromByte) / chunks
        var onDisk = fromByte

        repeat(times = chunks) { index ->
            onDisk = if (index == chunks - 1) file.sizeBytes else onDisk + step
            storage.partial[file.fileName] = onDisk
            emit(value = DownloadStep.Received(bytesOnDisk = onDisk))

            if (index == 0 && file.fileName in failing) {
                emit(
                    value = DownloadStep.Finished(
                        outcome = Outcome.Failure(error = ModelError.Network)
                    )
                )

                return@flow
            }

            if (index == 0 && file.fileName in held) awaitCancellation()
        }

        emit(value = DownloadStep.Finished(outcome = Outcome.Success(value = Unit)))
    }
}

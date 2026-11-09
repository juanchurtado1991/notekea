package app.notekea.ai.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

/**
 * Installs [ChatModel] and the catalog's reranker as one: checks the disk has room, downloads each missing file from
 * where it stopped and checks its SHA-256, stopping at the first failure. A resumed installation passes the files it
 * [counted] when it began, so its total stays the same.
 */
class InstallModelUseCase(
    private val storage: ModelStorage,
    private val downloader: ModelDownloader
) {
    operator fun invoke(
        model: ChatModel,
        reranker: ModelFile,
        counted: List<ModelFile>? = null
    ): Flow<InstallProgress> = flow {
        val missing = listOf(reranker, model.file).filterNot { storage.isInstalled(file = it) }

        val partial = missing.associateWith { storage.partialBytes(file = it) }

        val countedFiles = counted ?: missing
        val totalBytes = countedFiles.sumOf { it.sizeBytes }
        val neededBytes = missing.sumOf { it.sizeBytes } - partial.values.sum()
        val freeBytes = storage.freeBytes()

        if (freeBytes < neededBytes + SPARE_BYTES) {
            emit(
                value = InstallProgress.Failed(
                    error = ModelError.NotEnoughDisk(
                        neededBytes =
                        neededBytes + SPARE_BYTES - freeBytes
                    )
                )
            )

            return@flow
        }

        var doneBefore = (countedFiles - missing.toSet()).sumOf { it.sizeBytes }

        for (file in missing) {
            val failure = install(
                file = file,
                fromByte = partial.getValue(key = file),
                doneBefore = doneBefore,
                totalBytes = totalBytes
            )

            if (failure != null) {
                emit(value = InstallProgress.Failed(error = failure))

                return@flow
            }

            doneBefore += file.sizeBytes
        }

        emit(value = InstallProgress.Installed)
    }

    /** Downloads and verifies one file, reporting its bytes after the [doneBefore] of the files before it. */
    private suspend fun FlowCollector<InstallProgress>.install(
        file: ModelFile,
        fromByte: Long,
        doneBefore: Long,
        totalBytes: Long
    ): ModelError? {
        emit(
            value = InstallProgress.Downloading(
                doneBytes = doneBefore + fromByte,
                totalBytes = totalBytes
            )
        )

        var failure: ModelError? = null

        if (fromByte < file.sizeBytes) {
            downloader.download(
                file = file,
                fromByte = fromByte
            ).collect { step ->
                when (step) {
                    is DownloadStep.Received -> emit(
                        value = InstallProgress.Downloading(
                            doneBytes = doneBefore + step.bytesOnDisk,
                            totalBytes = totalBytes
                        )
                    )

                    is DownloadStep.Finished -> failure = (step.outcome as? Outcome.Failure)?.error
                }
            }
        }

        if (failure != null) return failure
        emit(value = InstallProgress.Verifying)

        return (storage.verifyAndInstall(file = file) as? Outcome.Failure)?.error
    }

    private companion object {
        /** Room left on the disk after the download, so the models never fill it. */
        const val SPARE_BYTES = 500_000_000L
    }
}

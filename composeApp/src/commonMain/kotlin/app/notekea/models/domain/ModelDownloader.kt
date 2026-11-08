package app.notekea.models.domain

import kotlinx.coroutines.flow.Flow

/** Downloads a [ModelFile] into the models folder, resuming from the bytes already there. */
interface ModelDownloader {
    /**
     * Emits the bytes of [file] on the disk as they arrive, then the outcome. Cancelling the collection pauses it:
     * the bytes stay for the next call.
     */
    fun download(
        file: ModelFile,
        fromByte: Long
    ): Flow<DownloadStep>
}

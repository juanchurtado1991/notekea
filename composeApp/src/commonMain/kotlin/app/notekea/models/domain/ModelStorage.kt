package app.notekea.models.domain

import app.notekea.core.domain.Outcome

/**
 * The models folder. A file is only under its own name once its SHA-256 matched, so [isInstalled] needs no new check;
 * until then its bytes are kept apart ([partialBytes]), so a paused download resumes where it stopped.
 */
interface ModelStorage {
    /** Where the models are kept, as shown to the person. */
    val folder: String

    suspend fun isInstalled(
        file: ModelFile
    ): Boolean

    /** The bytes of [file] downloaded so far, 0 when none. */
    suspend fun partialBytes(
        file: ModelFile
    ): Long

    /** Free space on the disk of the models folder. */
    suspend fun freeBytes(): Long

    /** Checks the downloaded bytes against [ModelFile.sha256] and installs them, or deletes them when they differ. */
    suspend fun verifyAndInstall(
        file: ModelFile
    ): Outcome<Unit, ModelError>

    /** Deletes [file], installed or partly downloaded. */
    suspend fun delete(
        file: ModelFile
    )
}

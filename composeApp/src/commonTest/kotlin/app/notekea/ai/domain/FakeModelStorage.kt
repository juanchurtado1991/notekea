package app.notekea.ai.domain

import app.notekea.core.domain.Outcome

/**
 * [ModelStorage] in memory: [partial] holds the downloaded bytes of each file and [installed] the verified ones;
 * [corrupted] files fail their checksum, as a damaged download does, and are deleted.
 */
class FakeModelStorage(
    var freeSpace: Long = Long.MAX_VALUE / 2
) : ModelStorage {
    val partial = mutableMapOf<String, Long>()
    val installed = mutableSetOf<String>()
    val corrupted = mutableSetOf<String>()
    val deleted = mutableListOf<String>()

    override val folder: String = "/data/notekea/models"

    override suspend fun isInstalled(
        file: ModelFile
    ): Boolean = file.fileName in installed

    override suspend fun partialBytes(
        file: ModelFile
    ): Long = partial[file.fileName] ?: 0

    override suspend fun freeBytes(): Long = freeSpace

    override suspend fun verifyAndInstall(
        file: ModelFile
    ): Outcome<Unit, ModelError> {
        partial.remove(key = file.fileName)
        if (file.fileName in corrupted) return Outcome.Failure(error = ModelError.ChecksumMismatch)
        installed += file.fileName
        return Outcome.Success(value = Unit)
    }

    override suspend fun delete(
        file: ModelFile
    ) {
        deleted += file.fileName
        installed -= file.fileName
        partial.remove(key = file.fileName)
    }
}

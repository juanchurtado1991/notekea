package app.notekea.sync.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.sync.domain.VaultSizes
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/** [VaultSizes] with Okio: every file under the vault added up, its `.git` folder left out, links not followed. */
class OkioVaultSizes(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers
) : VaultSizes {
    override suspend fun bytesOf(vaultPath: String): Long = withContext(context = dispatchers.io) {
        sizeOf(folder = vaultPath.toPath())
    }

    private fun sizeOf(folder: Path): Long = fileSystem
        .listOrNull(dir = folder)
        .orEmpty()
        .filterNot {
            it.name == GIT_FOLDER
        }
        .sumOf { path ->
            val metadata = fileSystem.metadataOrNull(path = path)

            when {
                metadata == null || metadata.symlinkTarget != null -> 0L
                metadata.isDirectory -> sizeOf(folder = path)
                else -> metadata.size ?: 0L
            }
        }

    private companion object {
        const val GIT_FOLDER = ".git"
    }
}

package app.notekea.vault.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.vault.domain.SampleVaultSource
import app.notekea.vault.domain.VaultError
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * [SampleVaultSource] from the app's own files: the build bundles the repository's Sample Vault with an `index.txt` of
 * its files, since a jar cannot list a folder, and each is copied as it is.
 */
class BundledSampleVault(private val dispatchers: AppDispatchers) : SampleVaultSource {
    override suspend fun copyTo(folder: String): Outcome<Unit, VaultError> =
        withContext(context = dispatchers.io) {
            try {
                index().forEach { relative ->
                    val file = Path.of(folder).resolve(relative)
                    Files.createDirectories(file.parent)

                    resource(path = relative).use {
                        Files.copy(it, file, StandardCopyOption.REPLACE_EXISTING)
                    }
                }

                Outcome.Success(value = Unit)
            } catch (_: IOException) {
                Outcome.Failure(error = VaultError.CreationFailed(path = folder))
            }
        }

    private fun index(): List<String> = resource(path = INDEX)
        .bufferedReader()
        .use { it.readLines() }
        .filter(String::isNotBlank)

    private fun resource(path: String) =
        BundledSampleVault::class.java.getResourceAsStream(ROOT + path)
            ?: throw IOException("The sample vault lacks $path")

    private companion object {
        const val ROOT = "/sample-vault/"
        const val INDEX = "index.txt"
    }
}

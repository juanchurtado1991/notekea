package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/**
 * The sample vault Notekea ships (5.1's "Try the sample vault"): books, recipes, projects, meetings and a journal that
 * show what Notekea does, copied into an empty folder the person then owns.
 */
fun interface SampleVaultSource {
    suspend fun copyTo(folder: String): Outcome<Unit, VaultError>
}

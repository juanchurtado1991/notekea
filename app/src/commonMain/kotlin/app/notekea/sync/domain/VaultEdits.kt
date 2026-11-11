package app.notekea.sync.domain

import kotlinx.coroutines.flow.Flow

/** A signal each time something in a vault changes on disk, so sync commits after a pause; the shell provides it. */
fun interface VaultEdits {
    fun of(vaultPath: String): Flow<Unit>
}

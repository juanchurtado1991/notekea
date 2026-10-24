package app.notekea.notes.domain

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** A watcher the test drives: every [emit] reaches the collector of [watch]. */
class FakeFileWatcher : FileWatcher {
    private val changes = Channel<VaultChange>(capacity = Channel.UNLIMITED)

    suspend fun emit(
        change: VaultChange
    ) {
        changes.send(element = change)
    }

    override fun watch(
        vaultPath: String
    ): Flow<VaultChange> = changes.receiveAsFlow()
}

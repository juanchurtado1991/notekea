package app.notekea.sync.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.MutableStateFlow

/** A [VaultSync] whose [status] the test sets, recording what it was asked. */
class FakeVaultSync : VaultSync {
    override val status = MutableStateFlow<SyncStatus>(value = SyncStatus.NotConfigured)
    val calls = mutableListOf<String>()
    var connectFailure: SyncError? = null

    override fun start() {
        calls += "start"
    }

    override fun stop() {
        calls += "stop"
    }

    override suspend fun syncNow() {
        calls += "sync"
    }

    override suspend fun connect(
        settings: SyncSettings,
        token: String?
    ): Outcome<Unit, SyncError> {
        calls += "connect ${settings.remoteUrl} $token"

        return connectFailure?.let { Outcome.Failure(error = it) } ?: Outcome.Success(value = Unit)
    }

    override suspend fun disconnect() {
        calls += "disconnect"
    }

    override suspend fun change(settings: SyncSettings) {
        calls += "change"
    }

    override suspend fun resolve(
        note: RelativePath,
        choice: ConflictChoice
    ) {
        calls += "resolve ${note.value} $choice"
    }
}

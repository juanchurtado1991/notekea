package app.notekea.sync.data

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.GitRepository
import app.notekea.sync.domain.RemoteAddress
import app.notekea.sync.domain.SyncError
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncSettingsStore
import app.notekea.sync.domain.SyncStatus
import app.notekea.sync.domain.TokenStore
import app.notekea.sync.domain.VaultEdits
import app.notekea.sync.domain.VaultSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * One vault's sync (4.1) while it is open: a pull when it opens and every interval, a commit [commitAfter] the last
 * edit followed by a pull and a push, one cycle at a time. Offline it waits for the next try without a word; a
 * refused token or a conflict shows in [status] until it is settled.
 */
@OptIn(FlowPreview::class)
class SyncEngine(
    private val vaultPath: String,
    private val repository: GitRepository,
    private val tokens: TokenStore,
    private val settingsStore: SyncSettingsStore,
    private val edits: VaultEdits,
    private val scope: CoroutineScope,
    private val commitAfter: Duration = 10.seconds
) : VaultSync {
    private val mutableStatus = MutableStateFlow<SyncStatus>(value = SyncStatus.NotConfigured)
    private val cycle = Mutex()
    private var jobs: List<Job> = emptyList()

    override val status: StateFlow<SyncStatus> = mutableStatus.asStateFlow()

    override fun start() {
        stop()

        jobs = listOf(
            scope.launch {
                val settings = settingsStore.read(vaultPath = vaultPath) ?: return@launch

                launch {
                    edits
                        .of(vaultPath = vaultPath)
                        .debounce(timeout = commitAfter)
                        .collect { afterEdits() }
                }

                syncNow(settings = settings)
                tick()
            }
        )
    }

    override fun stop() {
        jobs.forEach { it.cancel() }
        jobs = emptyList()
    }

    override suspend fun syncNow() {
        val settings = settingsStore.read(vaultPath = vaultPath) ?: return run {
            mutableStatus.value = NOT_SET
        }

        syncNow(settings = settings)
    }

    override suspend fun connect(
        settings: SyncSettings,
        token: String?
    ): Outcome<Unit, SyncError> = connectTo(
        address = RemoteAddress.of(written = settings.remoteUrl),
        written = settings,
        token = token
    )

    // The address without the token it may carry, which is kept as the token when none was typed.
    private suspend fun connectTo(
        address: RemoteAddress,
        written: SyncSettings,
        token: String?
    ): Outcome<Unit, SyncError> = cycle.withLock {
        val settings = written.copy(remoteUrl = address.url)
        mutableStatus.value = SyncStatus.Syncing

        (token?.takeIf { it.isNotBlank() } ?: address.secret)?.let {
            tokens.save(
                remoteUrl = settings.remoteUrl,
                token = it
            )
        }

        val saved = tokens.read(remoteUrl = settings.remoteUrl)

        when (val connected = repository.connect(vaultPath, settings.remoteUrl, saved)) {
            is Outcome.Success -> {
                settingsStore.write(
                    vaultPath = vaultPath,
                    settings = settings
                )

                mutableStatus.value = settled()
                Outcome.Success(value = Unit)
            }

            is Outcome.Failure -> {
                mutableStatus.value = statusOf(error = connected.error)
                Outcome.Failure(error = connected.error)
            }
        }
    }.also {
        if (it is Outcome.Success) start()
    }

    override suspend fun disconnect() {
        stop()
        settingsStore.read(vaultPath = vaultPath)?.let { tokens.delete(remoteUrl = it.remoteUrl) }

        settingsStore.write(
            vaultPath = vaultPath,
            settings = null
        )

        mutableStatus.value = NOT_SET
    }

    override suspend fun change(settings: SyncSettings) {
        settingsStore.write(
            vaultPath = vaultPath,
            settings = settings
        )

        start()
    }

    override suspend fun resolve(
        note: RelativePath,
        choice: ConflictChoice
    ) {
        cycle.withLock {
            repository.resolve(
                vaultPath = vaultPath,
                note = note,
                choice = choice
            )
        }

        syncNow()
    }

    private suspend fun tick() {
        while (scope.isActive) {
            val settings = settingsStore.read(vaultPath = vaultPath) ?: return
            delay(duration = settings.intervalMinutes.minutes)
            if (settings.isAuto) syncNow(settings = settings)
        }
    }

    private suspend fun afterEdits() {
        val settings = settingsStore.read(vaultPath = vaultPath) ?: return

        if (settings.isAuto) {
            syncNow(settings = settings)
        } else {
            mutableStatus.value = settled()
        }
    }

    private suspend fun syncNow(settings: SyncSettings) = cycle.withLock {
        val before = mutableStatus.value
        mutableStatus.value = SyncStatus.Syncing

        // Whatever ends the cycle (stopped half way, or something thrown), the ring stops turning.
        try {
            val token = tokens.read(remoteUrl = settings.remoteUrl)

            val error = SyncCycle.run(
                repository = repository,
                vaultPath = vaultPath,
                token = token
            )

            mutableStatus.value = error?.let { statusOf(error = it) } ?: settled()
        } finally {
            if (mutableStatus.value == SyncStatus.Syncing) mutableStatus.value = before
        }
    }

    /** Where the vault stands once a cycle went through: a conflict, changes not pushed, or up to date. */
    private suspend fun settled(): SyncStatus {
        val conflicts = repository.conflicts(vaultPath = vaultPath)
        if (conflicts.isNotEmpty()) return SyncStatus.Conflict(notes = conflicts)

        val pending = repository.pendingChanges(vaultPath = vaultPath)

        return if (pending > 0) SyncStatus.Pending(changes = pending) else SyncStatus.UpToDate
    }

    private suspend fun statusOf(error: SyncError): SyncStatus = when (error) {
        SyncError.AuthFailed -> SyncStatus.AuthFailed

        SyncError.Offline -> SyncStatus.Offline

        SyncError.BadAddress -> SyncStatus.Failed(message = BAD_ADDRESS)

        is SyncError.Failed ->
            repository
                .conflicts(vaultPath = vaultPath)
                .takeIf { it.isNotEmpty() }
                ?.let { SyncStatus.Conflict(notes = it) }
                ?: SyncStatus.Failed(message = error.message)
    }

    private companion object {
        val NOT_SET = SyncStatus.NotConfigured
        const val BAD_ADDRESS = "This is not the address of a Git repository"
    }
}

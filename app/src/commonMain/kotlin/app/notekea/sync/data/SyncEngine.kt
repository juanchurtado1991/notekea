package app.notekea.sync.data

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.GitRepository
import app.notekea.sync.domain.RemoteAddress
import app.notekea.sync.domain.RepairChoice
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

        // Connecting commits the vault as it is: never Git's conflict marks, sent from there to every device.
        if (isInterrupted()) return@withLock Outcome.Failure(error = INTERRUPTED)

        val typed = token?.takeIf { it.isNotBlank() } ?: address.secret

        typed?.let {
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
                // A token just typed that the remote refused is not kept: left there it was sent again when the
                // field was later left empty, for a repository that needs none (code review, 2026-11-14).
                val isRefused = typed != null && connected.error == SyncError.AuthFailed
                if (isRefused) tokens.delete(remoteUrl = settings.remoteUrl)

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
        val resolved = cycle.withLock {
            // Settling a conflict commits the vault: the merge cut short is repaired first.
            if (isInterrupted()) return

            repository.resolve(
                vaultPath = vaultPath,
                note = note,
                choice = choice
            )
        }

        // A conflict that could not be settled says why, as a repair that failed does.
        if (resolved is Outcome.Failure) {
            mutableStatus.value = statusOf(error = resolved.error)

            return
        }

        syncNow()
    }

    override suspend fun repair(choice: RepairChoice) {
        val repaired = cycle.withLock {
            repository.repair(
                vaultPath = vaultPath,
                choice = choice
            )
        }

        // A repair that failed says why, rather than the vault showing as interrupted again with no reason.
        if (repaired is Outcome.Failure) {
            mutableStatus.value = statusOf(error = repaired.error)

            return
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
            if (isInterrupted()) return@withLock

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

    /**
     * True, and the status says so, for a vault left in the middle of a merge or a rebase: nothing is committed then,
     * as a commit would hold Git's conflict marks.
     */
    private suspend fun isInterrupted(): Boolean {
        val unfinished = repository.unfinished(vaultPath = vaultPath) ?: return false
        mutableStatus.value = SyncStatus.Interrupted(changedHere = unfinished.changedHere)

        return true
    }

    /** Where the vault stands once a cycle went through: a conflict, changes not pushed, or up to date. */
    private suspend fun settled(): SyncStatus {
        repository.unfinished(vaultPath = vaultPath)?.let {
            return SyncStatus.Interrupted(changedHere = it.changedHere)
        }

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
        const val INTERRUPTED_MESSAGE =
            "This vault was left in the middle of a sync: repair it first"
        val INTERRUPTED = SyncError.Failed(message = INTERRUPTED_MESSAGE)
    }
}

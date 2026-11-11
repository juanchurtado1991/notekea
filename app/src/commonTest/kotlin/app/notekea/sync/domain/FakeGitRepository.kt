package app.notekea.sync.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.delay

/**
 * A [GitRepository] that records what it was asked and answers as set: [pullError] and [pushErrors] fail those
 * steps (each push error once), [conflictsAfterPull] are left by the next pull, [pending] files wait to be pushed.
 */
class FakeGitRepository : GitRepository {
    val calls = mutableListOf<String>()
    var connectError: SyncError? = null
    var pullError: SyncError? = null
    val pushErrors = ArrayDeque<SyncError>()
    var conflictsAfterPull: List<RelativePath> = emptyList()
    var pending = 0
    private var conflicts: List<RelativePath> = emptyList()

    override suspend fun connect(
        vaultPath: String,
        remoteUrl: String,
        token: String?
    ): Outcome<PullResult, SyncError> {
        delay(timeMillis = 1)
        calls += "connect $remoteUrl $token"

        connectError?.let {
            return Outcome.Failure(error = it)
        }

        return Outcome.Success(
            value = PullResult(
                changed = 0,
                conflicts = emptyList()
            )
        )
    }

    override suspend fun commitAll(
        vaultPath: String,
        message: String
    ): Outcome<Boolean, SyncError> {
        delay(timeMillis = 1)
        calls += "commit"

        return Outcome.Success(value = true)
    }

    override suspend fun pull(
        vaultPath: String,
        token: String?
    ): Outcome<PullResult, SyncError> {
        delay(timeMillis = 1)
        calls += "pull $token"

        pullError?.let {
            return Outcome.Failure(error = it)
        }

        conflicts = conflicts + conflictsAfterPull

        return Outcome.Success(
            value = PullResult(
                changed = 1,
                conflicts = conflictsAfterPull
            )
        )
            .also { conflictsAfterPull = emptyList() }
    }

    override suspend fun push(
        vaultPath: String,
        token: String?
    ): Outcome<Unit, SyncError> {
        delay(timeMillis = 1)
        calls += "push"

        pushErrors.removeFirstOrNull()?.let {
            return Outcome.Failure(error = it)
        }

        pending = 0

        return Outcome.Success(value = Unit)
    }

    override suspend fun pendingChanges(vaultPath: String): Int = pending

    override suspend fun conflicts(vaultPath: String): List<RelativePath> = conflicts

    override suspend fun resolve(
        vaultPath: String,
        note: RelativePath,
        choice: ConflictChoice
    ): Outcome<Unit, SyncError> {
        calls += "resolve ${note.value} $choice"
        conflicts = conflicts - note

        return Outcome.Success(value = Unit)
    }
}

package app.notekea.sync.data

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.GitRepository
import app.notekea.sync.domain.SyncError

/**
 * One sync: commit what changed, pull, push. A push refused because the remote moved meanwhile is tried once more
 * after another pull. Returns what stopped it, or null; the files the commit left out for their size go to
 * [run]'s `onLeftOut`.
 */
internal object SyncCycle {
    private const val MESSAGE = "Notekea: notes changed"

    suspend fun run(
        repository: GitRepository,
        vaultPath: String,
        token: String?,
        onLeftOut: (List<RelativePath>) -> Unit
    ): SyncError? {
        val committed = repository.commitAll(
            vaultPath = vaultPath,
            message = MESSAGE
        )

        when (committed) {
            is Outcome.Failure -> return committed.error

            is Outcome.Success ->
                committed
                    .value
                    .leftOut
                    .takeIf { it.isNotEmpty() }
                    ?.let(onLeftOut)
        }

        repeat(times = 2) { attempt ->
            repository
                .pull(
                    vaultPath = vaultPath,
                    token = token
                )
                .errorOrNull()
                ?.let {
                    return it
                }

            val pushed = repository.push(
                vaultPath = vaultPath,
                token = token
            )

            if (pushed is Outcome.Success) return null

            // Only a push refused by Git (the remote moved meanwhile) is worth one more pull.
            val error = (pushed as Outcome.Failure).error
            val isFinal = attempt == 1 || error !is SyncError.Failed
            if (isFinal) return error
        }

        return null
    }
}

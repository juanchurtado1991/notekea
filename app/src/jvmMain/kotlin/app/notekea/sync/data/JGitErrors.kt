package app.notekea.sync.data

import app.notekea.sync.domain.SyncError
import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.TransportException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** What JGit threw, as the [SyncError] the app shows: a refused token, no network, a bad address, or its message. */
internal object JGitErrors {
    private val AUTH_WORDS = listOf("not authorized", "authentication", "401", "403", "auth fail")

    fun of(throwable: Throwable): SyncError {
        val causes = generateSequence(seed = throwable) { it.cause }.toList()
        val messages = causes.mapNotNull { it.message?.lowercase() }

        val isOffline = causes.any {
            it is UnknownHostException ||
                it is ConnectException ||
                it is SocketTimeoutException ||
                it is NoRouteToHostException
        }

        val isAuth = throwable is TransportException &&
            messages.any { message ->
                AUTH_WORDS.any {
                    it in message
                }
            }

        return when {
            isOffline -> SyncError.Offline

            isAuth -> SyncError.AuthFailed

            throwable is InvalidRemoteException -> SyncError.BadAddress

            else -> SyncError.Failed(
                message = throwable.message ?: throwable::class.simpleName.orEmpty()
            )
        }
    }
}

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
    // What JGit says of a refused token (HTTP and SSH), and an HTTP status standing on its own.
    private val AUTH_WORDS = listOf(
        "not authorized",
        "authentication",
        "not permitted on",
        "auth fail"
    )
    private val AUTH_STATUS = Regex(pattern = "\\b40[13]\\b")
    private val SECRET = Regex(pattern = "://[^@/\\s]+@")

    // An address in a message, quoted or not: its path may hold anything, "401" and "403" too.
    private val ADDRESS = Regex(pattern = "'?[a-z][a-z0-9+.-]*:/\\S*")

    fun of(throwable: Throwable): SyncError {
        val causes = generateSequence(seed = throwable) { it.cause }.toList()

        val messages = causes.mapNotNull {
            it
                .message
                ?.lowercase()
                ?.replace(
                    regex = ADDRESS,
                    replacement = ""
                )
        }

        val isOffline = causes.any {
            it is UnknownHostException ||
                it is ConnectException ||
                it is SocketTimeoutException ||
                it is NoRouteToHostException
        }

        val isAuth = throwable is TransportException && messages.any { isRefusal(message = it) }

        return when {
            isOffline -> SyncError.Offline

            isAuth -> SyncError.AuthFailed

            throwable is InvalidRemoteException -> SyncError.BadAddress

            // An address written with its token ("https://token@host/…") is never shown or kept with it.
            else -> SyncError.Failed(
                message = (throwable.message ?: throwable::class.simpleName.orEmpty())
                    .replace(
                        regex = SECRET,
                        replacement = "://"
                    )
            )
        }
    }

    private fun isRefusal(message: String): Boolean {
        val isSaid = AUTH_WORDS.any {
            it in message
        }

        val isStatus = AUTH_STATUS.containsMatchIn(input = message)

        return isSaid || isStatus
    }
}

package app.notekea.sync.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.sync.domain.TokenStore
import com.github.javakeyring.Keyring
import kotlinx.coroutines.withContext

/**
 * [TokenStore] in the system's keychain (java-keyring: Keychain on macOS, Credential Manager on Windows, the Secret
 * Service on Linux), one entry per remote. A keychain that cannot be reached reads as no token.
 */
class KeyringTokenStore(private val dispatchers: AppDispatchers) : TokenStore {
    override suspend fun read(remoteUrl: String): String? = withKeyring {
        it.getPassword(SERVICE, remoteUrl)
    }

    override suspend fun save(
        remoteUrl: String,
        token: String
    ) {
        withKeyring { it.setPassword(SERVICE, remoteUrl, token) }
    }

    override suspend fun delete(remoteUrl: String) {
        withKeyring { it.deletePassword(SERVICE, remoteUrl) }
    }

    private suspend fun <T> withKeyring(block: (Keyring) -> T): T? =
        withContext(context = dispatchers.io) {
            try {
                Keyring.create().use { block(it) }
            } catch (exception: Exception) {
                if (exception is kotlinx.coroutines.CancellationException) throw exception
                null
            }
        }

    private companion object {
        const val SERVICE = "Notekea sync"
    }
}

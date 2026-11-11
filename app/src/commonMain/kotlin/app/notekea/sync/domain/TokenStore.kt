package app.notekea.sync.domain

/** Where the HTTPS token of a remote is kept: the system's keychain, never a file. */
interface TokenStore {
    suspend fun read(remoteUrl: String): String?

    suspend fun save(
        remoteUrl: String,
        token: String
    )

    suspend fun delete(remoteUrl: String)
}

package app.notekea.sync.domain

/** The keychain, in memory. */
class FakeTokenStore : TokenStore {
    val tokens = mutableMapOf<String, String>()

    override suspend fun read(remoteUrl: String): String? = tokens[remoteUrl]

    override suspend fun save(
        remoteUrl: String,
        token: String
    ) {
        tokens[remoteUrl] = token
    }

    override suspend fun delete(remoteUrl: String) {
        tokens -= remoteUrl
    }
}

package app.notekea.sync.domain

/**
 * GitHub's limits for a repository (its documentation, read 2026-11-11): it asks a repository to stay under about
 * 1 GB, which a vault with videos passes; a vault synced elsewhere, or kept on this computer only, is not told.
 */
object GitHubLimits {
    const val REPOSITORY_BYTES = 1_000_000_000L

    private const val HOST = "github.com"

    /** Whether the remote at [url] is on GitHub. */
    fun isGitHub(url: String): Boolean = url
        .substringAfter(delimiter = "://")
        .substringBefore(delimiter = '/')
        .substringAfter(delimiter = '@')
        .lowercase()
        .let {
            it == HOST || it == "www.$HOST"
        }

    /** Whether a vault of [bytes] synced with [url] may be too large for GitHub's free account. */
    fun isOver(
        url: String,
        bytes: Long?
    ): Boolean =
        bytes != null &&
            bytes > REPOSITORY_BYTES &&
            isGitHub(url = url.trim())
}

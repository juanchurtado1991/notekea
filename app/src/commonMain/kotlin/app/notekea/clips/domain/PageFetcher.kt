package app.notekea.clips.domain

import app.notekea.core.domain.Outcome

/**
 * Downloads a web page the user named (`GUIDELINES.md` §13): only http and https, no address on this computer or its
 * local network, each redirect checked the same way, with limits on size and time.
 */
fun interface PageFetcher {
    suspend fun fetch(url: String): Outcome<FetchedPage, ClipError>
}

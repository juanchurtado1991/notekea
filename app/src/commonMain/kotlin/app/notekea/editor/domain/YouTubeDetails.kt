package app.notekea.editor.domain

/**
 * A YouTube video's title and picture (#141): asked of YouTube once, when its link is pasted ([fetch]); its picture is
 * then kept in the app's data folder and read from there ([thumbnail]), so showing a note never goes on the network.
 */
interface YouTubeDetails {
    /** The title of the video [id], keeping its picture; null when YouTube cannot be reached or does not say. */
    suspend fun fetch(id: String): String?

    /** The picture kept for the video [id], read from the disk only; null when none was kept. */
    suspend fun thumbnail(id: String): ByteArray?
}

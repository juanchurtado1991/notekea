package app.notekea.editor.domain

import kotlinx.coroutines.flow.Flow

/**
 * Makes a video small enough for GitHub (100 MB a file, `GUIDELINES.md` §13 and #141): H.264 at the best quality that
 * fits, with FFmpeg, downloaded the first time and checked against its checksum. Collecting is what runs it; cancelling
 * the collection stops it.
 */
interface VideoCompressor {
    /** Whether the video file at [source] is over [maxBytes], and so is compressed before it is attached. */
    suspend fun isOverLimit(
        source: String,
        maxBytes: Long
    ): Boolean

    /** A copy of the video at [source] under [maxBytes], as an `.mp4` of the same name in a folder of its own. */
    fun compress(
        source: String,
        maxBytes: Long
    ): Flow<CompressStep>

    /** Throws away a compressed copy once it is attached. */
    suspend fun discard(path: String)
}

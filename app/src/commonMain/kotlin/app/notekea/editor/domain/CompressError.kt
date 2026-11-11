package app.notekea.editor.domain

/** Why a video could not be compressed; the video is then not attached, and the note is unchanged. */
sealed interface CompressError {
    /** FFmpeg could not be downloaded: no network, or its download did not match its checksum. */
    data object ToolUnavailable : CompressError

    /** FFmpeg could not read the video, or could not make it small enough. */
    data object Failed : CompressError
}

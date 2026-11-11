package app.notekea.editor.domain

/** Where compressing a video stands: FFmpeg downloading the first time, then the video itself, then its copy. */
sealed interface CompressStep {
    /** FFmpeg being downloaded, [fraction] of it so far. */
    data class Downloading(val fraction: Float) : CompressStep

    /** The video being compressed, [fraction] of its length so far. */
    data class Compressing(val fraction: Float) : CompressStep

    /** The compressed copy, at [path], to attach in place of the video. */
    data class Done(val path: String) : CompressStep

    data class Failed(val error: CompressError) : CompressStep
}

package app.notekea.editor.domain

/** GitHub's limit for one file (100 MB), which a video is kept under so a vault synced there never breaks. */
object VideoLimits {
    const val MAX_BYTES = 100_000_000L
}

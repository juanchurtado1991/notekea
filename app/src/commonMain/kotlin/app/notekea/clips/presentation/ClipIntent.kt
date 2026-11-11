package app.notekea.clips.presentation

sealed interface ClipIntent {
    /** "Clip web page…" in the editor's More menu: the dialog opens, empty. */
    data object OnOpen : ClipIntent

    data class OnUrlChange(val url: String) : ClipIntent

    data object OnClip : ClipIntent

    /** Cancel or Esc: a clip under way stops, and nothing is written. */
    data object OnDismiss : ClipIntent
}

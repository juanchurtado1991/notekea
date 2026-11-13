package app.notekea.clips.presentation

import app.notekea.core.notes.domain.RelativePath

sealed interface ClipIntent {
    /** "Clip web page…" in the editor's More menu: the dialog opens, empty. */
    data object OnOpen : ClipIntent

    data class OnUrlChange(val url: String) : ClipIntent

    data object OnClip : ClipIntent

    /** Asked about the page's section: only the page itself is clipped. */
    data object OnClipPage : ClipIntent

    /** Asked about the page's section: the page and the pages it links under its path are clipped. */
    data object OnClipSection : ClipIntent

    /** Asked about a section of more pages than are clipped by default: every one of them. */
    data object OnClipAll : ClipIntent

    /** Asked about the page's section: the pages its pages link too, [levels] levels of links in all. */
    data class OnClipDeep(val levels: Int) : ClipIntent

    /**
     * Update from the web, in the menu of the Web sources folder or of something in it: the clipped pages under [path]
     * are read again. Without [isShown] it is the automatic update, which works with no dialog.
     */
    data class OnUpdate(
        val path: RelativePath,
        val isShown: Boolean = true
    ) : ClipIntent

    /** The status bar's entry was clicked: the update under way shows its dialog. */
    data object OnUpdateShow : ClipIntent

    /** The update's dialog is closed; an update under way stops. */
    data object OnUpdateDismiss : ClipIntent

    /** Cancel or Esc: a clip under way stops, and nothing is written. */
    data object OnDismiss : ClipIntent
}

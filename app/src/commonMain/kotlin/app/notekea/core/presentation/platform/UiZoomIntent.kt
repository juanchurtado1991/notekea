package app.notekea.core.presentation.platform

/** What the user asked of the window's zoom. */
sealed interface UiZoomIntent {
    data object OnLarger : UiZoomIntent

    data object OnSmaller : UiZoomIntent

    /** Back to the system's size. */
    data object OnReset : UiZoomIntent
}

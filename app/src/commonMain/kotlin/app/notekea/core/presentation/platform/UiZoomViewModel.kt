package app.notekea.core.presentation.platform

import app.notekea.core.appearance.domain.GetUiZoomUseCase
import app.notekea.core.appearance.domain.SaveUiZoomUseCase
import app.notekea.core.appearance.domain.UiZoomLevels
import app.notekea.core.presentation.BaseViewModel

/**
 * The window's zoom: it starts where it was left and each change is kept for the next run. Its state
 * is the scale; one that cannot be read or kept only logs, and the window stays at the size it shows.
 */
class UiZoomViewModel(
    private val getUiZoom: GetUiZoomUseCase,
    private val saveUiZoom: SaveUiZoomUseCase
) : BaseViewModel<Float, UiZoomIntent, Nothing>(initialState = UiZoomLevels.DEFAULT) {

    // A zoom chosen before the kept one came back stays: the load put the old one over it (the
    // code review of 2026-11-22).
    private var isChanged = false

    init {
        launchLogged(message = UNEXPECTED_ERROR_MESSAGE) {
            val kept = getUiZoom()
            if (!isChanged) reduce { kept }
        }
    }

    override fun handleIntent(intent: UiZoomIntent) {
        val scale = state.value

        val newScale = when (intent) {
            UiZoomIntent.OnLarger -> UiZoomLevels.larger(scale = scale)
            UiZoomIntent.OnSmaller -> UiZoomLevels.smaller(scale = scale)
            UiZoomIntent.OnReset -> UiZoomLevels.DEFAULT
        }

        change(scale = newScale)
    }

    private fun change(scale: Float) {
        isChanged = true
        reduce { scale }

        launchLogged(message = UNEXPECTED_ERROR_MESSAGE) { saveUiZoom(scale = scale) }
    }

    private companion object {
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error with the window's zoom"
    }
}

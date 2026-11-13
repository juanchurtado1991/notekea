package app.notekea.core.presentation.platform

import app.notekea.core.appearance.domain.GetUiZoomUseCase
import app.notekea.core.appearance.domain.SaveUiZoomUseCase
import app.notekea.core.appearance.domain.UiZoomLevels
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger

/**
 * The window's zoom: it starts where it was left and each change is kept for the next run. Its state
 * is the scale; one that cannot be read or kept only logs, and the window stays at the size it shows.
 */
class UiZoomViewModel(
    private val getUiZoom: GetUiZoomUseCase,
    private val saveUiZoom: SaveUiZoomUseCase
) : BaseViewModel<Float, UiZoomIntent, Nothing>(initialState = UiZoomLevels.DEFAULT) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    init {
        launchGuarded(onError = ::logUnexpectedError) {
            val kept = getUiZoom()
            reduce { kept }
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
        reduce { scale }

        launchGuarded(onError = ::logUnexpectedError) { saveUiZoom(scale = scale) }
    }

    private fun logUnexpectedError(throwable: Throwable) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "UiZoomViewModel"
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error with the window's zoom"
    }
}

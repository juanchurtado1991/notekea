package app.notekea.core.presentation.platform

import app.notekea.core.appearance.domain.GetUiScaleUseCase
import app.notekea.core.appearance.domain.SaveUiScaleUseCase
import app.notekea.core.appearance.domain.UiScale
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger

/**
 * The window's zoom (`ROADMAP.md` 1.14): it starts where it was left and each change is kept for the next run. Its state
 * is the scale; one that cannot be read or kept only logs, and the window stays at the size it shows.
 */
class UiZoomViewModel(
    private val getUiScale: GetUiScaleUseCase,
    private val saveUiScale: SaveUiScaleUseCase
) : BaseViewModel<Float, UiZoomIntent, Nothing>(initialState = UiScale.DEFAULT) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    init {
        launchGuarded(onError = ::logUnexpectedError) {
            val kept = getUiScale()
            reduce { kept }
        }
    }

    override fun handleIntent(
        intent: UiZoomIntent
    ) {
        val scale = state.value
        change(
            scale = when (intent) {
                UiZoomIntent.OnLarger -> UiScale.larger(scale = scale)
                UiZoomIntent.OnSmaller -> UiScale.smaller(scale = scale)
                UiZoomIntent.OnReset -> UiScale.DEFAULT
            }
        )
    }

    private fun change(
        scale: Float
    ) {
        reduce { scale }
        launchGuarded(onError = ::logUnexpectedError) { saveUiScale(scale = scale) }
    }

    private fun logUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "UiZoomViewModel"
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error with the window's zoom"
    }
}

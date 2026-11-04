package app.notekea.core.presentation.platform

import app.notekea.core.appearance.domain.WindowBounds
import app.notekea.core.appearance.domain.WindowBoundsStore
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger

/** Keeps where the main window is left, for the next run: a window that cannot be kept simply opens as it does first. */
class WindowBoundsViewModel(
    private val windowBoundsStore: WindowBoundsStore
) : BaseViewModel<WindowBoundsState, WindowBoundsIntent, Nothing>(initialState = WindowBoundsState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    init {
        launchGuarded(onError = ::logUnexpectedError) {
            val kept = windowBoundsStore.bounds()
            reduce { state -> state.copy(bounds = state.bounds ?: kept) }
        }
    }

    override fun handleIntent(
        intent: WindowBoundsIntent
    ) {
        val bounds = when (intent) {
            is WindowBoundsIntent.OnFloating -> intent.bounds.copy(isMaximized = false)
            WindowBoundsIntent.OnMaximized -> state.value.bounds?.copy(isMaximized = true)
        }
        if (bounds != null) keep(bounds = bounds)
    }

    private fun keep(
        bounds: WindowBounds
    ) {
        reduce { it.copy(bounds = bounds) }
        launchGuarded(onError = ::logUnexpectedError) { windowBoundsStore.save(bounds = bounds) }
    }

    private fun logUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "WindowBoundsViewModel"
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error keeping the window's size"
    }
}

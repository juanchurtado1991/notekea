package app.notekea.core.presentation.platform

import app.notekea.core.appearance.domain.WindowBounds
import app.notekea.core.appearance.domain.WindowBoundsStore
import app.notekea.core.presentation.BaseViewModel

/**
 * Keeps where the main window is left, for the next run: a window that cannot be kept simply opens as it does first.
 */
class WindowBoundsViewModel(
    private val windowBoundsStore: WindowBoundsStore
) : BaseViewModel<WindowBoundsState, WindowBoundsIntent, Nothing>(
    initialState = WindowBoundsState()
) {

    init {
        launchLogged(message = UNEXPECTED_ERROR_MESSAGE) {
            val kept = windowBoundsStore.bounds()

            reduce { state -> state.copy(bounds = state.bounds ?: kept) }
        }
    }

    override fun handleIntent(intent: WindowBoundsIntent) {
        val bounds = when (intent) {
            is WindowBoundsIntent.OnFloating -> intent.bounds.copy(isMaximized = false)

            WindowBoundsIntent.OnMaximized ->
                state
                    .value
                    .bounds
                    ?.copy(isMaximized = true)
        }

        if (bounds != null) keep(bounds = bounds)
    }

    private fun keep(bounds: WindowBounds) {
        reduce { it.copy(bounds = bounds) }

        launchLogged(message = UNEXPECTED_ERROR_MESSAGE) { windowBoundsStore.save(bounds = bounds) }
    }

    private companion object {
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error keeping the window's size"
    }
}

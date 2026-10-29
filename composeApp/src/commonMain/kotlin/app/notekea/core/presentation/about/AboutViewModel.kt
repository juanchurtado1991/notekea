package app.notekea.core.presentation.about

import app.notekea.core.logging.domain.GetDiagnosticsUseCase
import app.notekea.core.logging.domain.TextClipboard
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger

/** What About shows besides its fixed texts: whether Copy diagnostics has put them on the clipboard. */
data class AboutState(
    val isDiagnosticsCopied: Boolean = false
)

sealed interface AboutIntent {
    data object OnCopyDiagnostics : AboutIntent
}

/** About Notekea's Copy diagnostics (`qa/README.md`, piece 6): the versions and the recent log, for a tester's report. */
class AboutViewModel(
    private val getDiagnostics: GetDiagnosticsUseCase,
    private val clipboard: TextClipboard
) : BaseViewModel<AboutState, AboutIntent, Nothing>(initialState = AboutState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    override fun onIntent(
        intent: AboutIntent
    ) {
        when (intent) {
            AboutIntent.OnCopyDiagnostics -> launchGuarded(onError = ::showUnexpectedError) {
                clipboard.copy(text = getDiagnostics())
                reduce { it.copy(isDiagnosticsCopied = true) }
            }
        }
    }

    private fun showUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "AboutViewModel"
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error copying the diagnostics"
    }
}

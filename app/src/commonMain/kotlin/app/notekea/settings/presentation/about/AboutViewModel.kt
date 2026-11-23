package app.notekea.settings.presentation.about

import app.notekea.core.logging.domain.TextClipboard
import app.notekea.core.presentation.BaseViewModel
import app.notekea.settings.domain.GetDiagnosticsUseCase

/**
 * About Notekea's Copy diagnostics (`qa/README.md`, piece 6): the versions and the recent log, for a tester's report.
 */
class AboutViewModel(
    private val getDiagnostics: GetDiagnosticsUseCase,
    private val clipboard: TextClipboard
) : BaseViewModel<AboutState, AboutIntent, Nothing>(initialState = AboutState()) {

    override fun handleIntent(intent: AboutIntent) {
        when (intent) {
            AboutIntent.OnCopyDiagnostics -> launchGuarded(onError = ::showUnexpectedError) {
                clipboard.copy(text = getDiagnostics())

                reduce {
                    it.copy(
                        isDiagnosticsCopied = true,
                        hasCopyFailed = false
                    )
                }
            }
        }
    }

    private fun showUnexpectedError(throwable: Throwable) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }

        reduce {
            it.copy(
                isDiagnosticsCopied = false,
                hasCopyFailed = true
            )
        }
    }

    private companion object {
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error copying the diagnostics"
    }
}

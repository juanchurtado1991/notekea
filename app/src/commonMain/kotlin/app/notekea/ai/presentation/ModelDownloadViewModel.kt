package app.notekea.ai.presentation

import app.notekea.ai.domain.ModelInstaller
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger

/**
 * The status bar's view of the chat model's installation, run by the [installer] in Settings → AI model (3.1), so its
 * progress stays in sight while Settings is closed (3.3).
 */
class ModelDownloadViewModel(private val installer: ModelInstaller) :
    BaseViewModel<ModelDownloadState, ModelDownloadIntent, ModelDownloadEffect>(
        initialState = ModelDownloadState()
    ) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    init {
        launchGuarded(onError = {
            logger.w(throwable = it) { FOLLOW_FAILED_MESSAGE }
        }) {
            installer.session.collect { session ->
                reduce { it.copy(session = session) }
            }
        }
    }

    override fun handleIntent(intent: ModelDownloadIntent) {
        when (intent) {
            ModelDownloadIntent.OnOpen -> emit(effect = ModelDownloadEffect.OpenSettings)
        }
    }

    private companion object {
        const val LOG_TAG = "ModelDownloadViewModel"
        const val FOLLOW_FAILED_MESSAGE = "The model's installation could not be followed"
    }
}

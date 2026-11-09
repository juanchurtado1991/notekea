package app.notekea.ai.presentation

import app.notekea.ai.domain.ModelSize

/** What the person does in Settings → AI model. */
sealed interface ModelManagerIntent {
    /** Download, resume or try again. */
    data class OnInstall(val size: ModelSize) : ModelManagerIntent

    data object OnPause : ModelManagerIntent

    data object OnCancel : ModelManagerIntent

    data class OnUse(val size: ModelSize) : ModelManagerIntent

    data class OnDeleteAsk(val size: ModelSize) : ModelManagerIntent

    data object OnDeleteConfirm : ModelManagerIntent

    data object OnDeleteDismiss : ModelManagerIntent

    data object OnOtherSizesToggle : ModelManagerIntent
}

package app.notekea.settings.presentation.memory

import app.notekea.core.memory.domain.MemoryField

/** What the person does in Settings → Chat memory. */
sealed interface MemorySettingsIntent {
    /** The page shows: the memory is read again, as the chat or the person may have changed it. */
    data object OnShown : MemorySettingsIntent

    data object OnToggle : MemorySettingsIntent

    data class OnFieldChange(
        val field: MemoryField,
        val value: String
    ) : MemorySettingsIntent

    /** A learnt line taken out, by its [text]. */
    data class OnForget(val text: String) : MemorySettingsIntent

    data object OnForgetAll : MemorySettingsIntent

    data object OnForgetAllConfirm : MemorySettingsIntent

    data object OnForgetAllCancel : MemorySettingsIntent
}

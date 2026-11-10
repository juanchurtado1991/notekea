package app.notekea.chat.presentation

import app.notekea.core.notes.domain.RelativePath

sealed interface ChatEffect {
    /** Opens [note] with [range] of its body selected. */
    data class Reveal(
        val note: RelativePath,
        val range: IntRange
    ) : ChatEffect

    /** Opens Settings → AI model. */
    data object OpenModelSettings : ChatEffect
}

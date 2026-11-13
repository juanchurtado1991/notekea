package app.notekea.chat.presentation.model

import app.notekea.chat.domain.ActionStep
import app.notekea.core.notes.domain.RelativePath

sealed interface ChatEffect {
    /** Opens [note] with [range] of its body selected. */
    data class Reveal(
        val note: RelativePath,
        val range: IntRange
    ) : ChatEffect

    /** Opens Settings → AI model. */
    data object OpenModelSettings : ChatEffect

    /** Opens [note], as a note the chat made or a row it found. */
    data class Open(val note: RelativePath) : ChatEffect

    /** Does what [step] shows (a note, a panel, the theme...): the window carries it out (3.9). */
    data class Show(val step: ActionStep) : ChatEffect
}

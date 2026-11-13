package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.FoundRow
import app.notekea.chat.presentation.model.ChatEffect

/** A task opens at its line; a note opens. */
internal fun foundEffectOf(row: FoundRow): ChatEffect = row.line?.let {
    ChatEffect.Reveal(
        note = row.note,
        range = it..it
    )
} ?: ChatEffect.Open(note = row.note)

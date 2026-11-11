package app.notekea.chat.presentation

import app.notekea.chat.domain.FoundRow

/** A task opens at its line; a note opens. */
internal fun foundEffectOf(row: FoundRow): ChatEffect = row.line?.let {
    ChatEffect.Reveal(
        note = row.note,
        range = it..it
    )
} ?: ChatEffect.Open(note = row.note)

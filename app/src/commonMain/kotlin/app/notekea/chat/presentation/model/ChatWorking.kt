package app.notekea.chat.presentation.model

import app.notekea.chat.domain.context.WorkContext

/**
 * The state once the person is at [working]: a chip removed stays removed while what it names is the same (the same
 * place, the same selection) and comes back when the person moves on.
 */
fun ChatState.at(working: WorkContext?): ChatState {
    val before = this.working
    val isSamePlace = before?.place == working?.place && before?.name == working?.name

    return copy(
        working = working,
        isPlaceHidden = isPlaceHidden && isSamePlace,
        isSelectedHidden = isSelectedHidden &&
            isSamePlace &&
            before?.selected == working?.selected
    )
}

/** The state with the chip of [part] removed. */
fun ChatState.without(part: WorkPart): ChatState = when (part) {
    WorkPart.PLACE -> copy(isPlaceHidden = true)
    WorkPart.SELECTED -> copy(isSelectedHidden = true)
}

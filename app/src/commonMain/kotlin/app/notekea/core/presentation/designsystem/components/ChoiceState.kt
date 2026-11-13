package app.notekea.core.presentation.designsystem.components

/** What a [NotekeaChoiceRow] says: not chosen yet, the right one, the wrong one chosen, or one of the others. */
enum class ChoiceState {
    IDLE,
    CORRECT,
    WRONG,
    DIMMED;

    companion object {
        /** The state of the option at [index] of a question whose right one is at [correct], once [chosen]. */
        fun of(
            index: Int,
            correct: Int,
            chosen: Int?
        ): ChoiceState = when {
            chosen == null -> IDLE
            index == correct -> CORRECT
            index == chosen -> WRONG
            else -> DIMMED
        }
    }
}

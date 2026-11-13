package app.notekea.chat.presentation.model

/** A step an answer went through and left ([step], having read [readNotes] notes): a checked line of its list. */
data class PassedStep(
    val step: ChatStep,
    val readNotes: Int
)

package app.notekea.chat.presentation.model

/** A move agent mode made (4.7), as the chat lists it: what it did ([move], as the model names it) and on what. */
data class AgentMoveLine(
    val move: String,
    val target: String
)

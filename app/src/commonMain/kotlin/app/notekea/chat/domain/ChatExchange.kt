package app.notekea.chat.domain

/** A question asked earlier in the chat and the start of its [reply], so a follow-up knows what "it" was. */
data class ChatExchange(
    val question: String,
    val reply: String
)

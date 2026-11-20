package app.notekea.chat.domain.loop

/**
 * The frame's first answer (`docs/agent-frame.md`): how many things a task means, its own [words] for them, and
 * whether the request changes an item already inside a note ([isItem]: an appointment, an order).
 */
internal data class FrameCountReply(
    val count: FrameCount,
    val words: String,
    val isItem: Boolean
)

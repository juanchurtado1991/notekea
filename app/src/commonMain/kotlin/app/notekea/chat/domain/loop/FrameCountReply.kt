package app.notekea.chat.domain.loop

/**
 * The frame's first answer (`docs/agent-frame.md`): how many things a task means, its own [words] for them, and
 * whether the one thing is an item inside a note ([isItem]: an appointment, an order) rather than a whole note.
 */
internal data class FrameCountReply(
    val count: FrameCount,
    val words: String,
    val isItem: Boolean
)

package app.notekea.chat.domain.loop

/**
 * The frame's first answer (`docs/agent-frame.md`): how many things a task means, its own [words] for them, [which]
 * of those so named ("the one from the chess club"), and whether the one is an item inside a note ([isItem]).
 */
internal data class FrameCountReply(
    val count: FrameCount,
    val words: String,
    val which: String,
    val isItem: Boolean
)

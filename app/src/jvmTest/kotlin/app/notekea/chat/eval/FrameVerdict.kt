package app.notekea.chat.eval

import app.notekea.chat.domain.loop.TaskFrame

/** One task's frame against what the task needs (`docs/loop-goal-plan.md`, step 1.2): asked back or not. */
internal class FrameVerdict(
    val task: String,
    val mustAsk: Boolean,
    val calls: List<TracedCall>,
    val frame: TaskFrame?
) {
    enum class Kind(val written: String) {
        HIT(written = "open, and must ask"),
        MISS(written = "not open, and must ask"),
        FALSE_ALARM(written = "open, and must not ask"),
        QUIET(written = "not open, and must not ask")
    }

    val isOpen: Boolean = frame?.isOpen == true

    val kind: Kind = when {
        mustAsk && isOpen -> Kind.HIT
        mustAsk -> Kind.MISS
        isOpen -> Kind.FALSE_ALARM
        else -> Kind.QUIET
    }
}

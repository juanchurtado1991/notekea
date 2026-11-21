package app.notekea.chat.domain.context

/** The [chunk]th part of a turn's [entry]th result. */
data class StepChunk(
    val entry: Int,
    val chunk: Int
)

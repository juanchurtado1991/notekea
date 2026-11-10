package app.notekea.core.llm.domain

/** A reply that stopped with [error]: what a stream of the [LlmEngine] fails with. */
class LlmException(val error: LlmError) : Exception(error.toString())

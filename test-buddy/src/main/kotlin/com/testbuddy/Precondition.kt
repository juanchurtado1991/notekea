package com.testbuddy

/**
 * What an automated case needs that not every machine has (a downloaded model, a device): while [isMet] says no, the
 * case is skipped instead of failed, and the report says it needs [missing].
 */
public class Precondition(
    public val missing: String,
    internal val isMet: () -> Boolean
)

/** Whether this case can run here: it needs nothing, or what it needs is there. */
internal fun AutomatedCase.canRun(): Boolean = precondition?.isMet?.invoke() ?: true

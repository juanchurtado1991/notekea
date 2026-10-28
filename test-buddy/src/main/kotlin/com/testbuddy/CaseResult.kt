package com.testbuddy

import java.nio.file.Path
import kotlin.time.Duration

/** What came of one automated case. */
public sealed class CaseResult {
    public abstract val id: String

    public data class Passed(
        override val id: String,
        val duration: Duration
    ) : CaseResult()

    /** The case did not hold: [message] says what was expected, [screenshot] shows the app when it failed. */
    public data class Failed(
        override val id: String,
        val message: String,
        val duration: Duration,
        val screenshot: Path? = null
    ) : CaseResult()

    /** The case did not run, because the run was stopped before it. */
    public data class Skipped(override val id: String) : CaseResult()
}

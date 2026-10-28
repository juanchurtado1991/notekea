package com.testbuddy

/** What a tester answered for one manual case. */
public sealed class ManualAnswer {
    public abstract val id: String

    public data class Passed(override val id: String) : ManualAnswer()

    /** The tester did not see what was expected: [note] says what they saw, [screenshot] names the file they added. */
    public data class Failed(
        override val id: String,
        val note: String,
        val screenshot: String? = null
    ) : ManualAnswer()

    public data class NotRun(override val id: String) : ManualAnswer()
}

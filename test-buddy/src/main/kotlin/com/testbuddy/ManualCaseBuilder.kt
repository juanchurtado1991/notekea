package com.testbuddy

/** Describes a [ManualCase]: what to prepare, and the texts the tester reads, in plain English. */
@TestBuddyDsl
public class ManualCaseBuilder internal constructor(
    private val id: String,
    private val reason: ManualReason
) {
    private val data = mutableListOf<String>()
    private var title: String? = null
    private var steps: String? = null
    private var expected: String? = null
    private var passLabel = DEFAULT_PASS
    private var failLabel = DEFAULT_FAIL
    private val launchEnvironment = mutableMapOf<String, String>()

    /** The folders of the suite's data this case needs, prepared before the tester starts it. */
    public fun prepare(vararg folders: String) {
        data.addAll(folders)
    }

    public fun title(text: String) {
        title = text
    }

    public fun steps(text: String) {
        steps = text
    }

    public fun expected(text: String) {
        expected = text
    }

    /** The words of the two answers when Passed and Failed do not fit: "Nothing odd" and "I found something". */
    public fun answers(
        passed: String,
        failed: String
    ) {
        passLabel = passed
        failLabel = failed
    }

    /** A variable the "Open <app>" button sets for this case only: `launchWith("NOTES_CRASH_TEST", "1")`. */
    public fun launchWith(
        name: String,
        value: String
    ) {
        launchEnvironment[name] = value
    }

    internal fun build(): ManualCase = ManualCase(
        id = id,
        reason = reason,
        data = data.toList(),
        title = requireNotNull(title) { "Manual case $id has no title" },
        steps = requireNotNull(steps) { "Manual case $id has no steps" },
        expected = requireNotNull(expected) { "Manual case $id has no expected result" },
        passLabel = passLabel,
        failLabel = failLabel,
        launchEnvironment = launchEnvironment.toMap()
    )

    private companion object {
        const val DEFAULT_PASS = "Passed"
        const val DEFAULT_FAIL = "Failed"
    }
}

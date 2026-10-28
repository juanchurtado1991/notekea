package com.testbuddy

/** Describes a [ManualCase]: what to prepare, and the texts the tester reads. */
@TestBuddyDsl
public class ManualCaseBuilder internal constructor(
    private val id: String,
    private val reason: ManualReason
) {
    private val data = mutableListOf<String>()
    private var title: CaseText? = null
    private var steps: CaseText? = null
    private var expected: CaseText? = null

    /** The folders of the suite's data this case needs, prepared before the tester starts it. */
    public fun prepare(vararg folders: String) {
        data.addAll(folders)
    }

    public fun title(text: CaseText) {
        title = text
    }

    public fun steps(text: CaseText) {
        steps = text
    }

    public fun expected(text: CaseText) {
        expected = text
    }

    internal fun build(): ManualCase = ManualCase(
        id = id,
        reason = reason,
        data = data.toList(),
        title = requireNotNull(title) { "Manual case $id has no title" },
        steps = requireNotNull(steps) { "Manual case $id has no steps" },
        expected = requireNotNull(expected) { "Manual case $id has no expected result" }
    )
}

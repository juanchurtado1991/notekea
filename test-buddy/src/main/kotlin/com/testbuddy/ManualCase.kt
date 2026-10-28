package com.testbuddy

/**
 * A case a person checks, walked through one screen at a time: the [data] folders it needs are prepared first, then
 * the tester reads [title], follows [steps] and says whether they saw [expected].
 */
public class ManualCase internal constructor(
    public val id: String,
    public val reason: ManualReason,
    public val data: List<String>,
    public val title: CaseText,
    public val steps: CaseText,
    public val expected: CaseText
)

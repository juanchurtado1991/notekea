package com.testbuddy

/**
 * A case a person checks, walked through one screen at a time: the [data] folders it needs are prepared first, then
 * the tester reads [title], follows [steps] and says whether they saw [expected], with the buttons [passLabel] and
 * [failLabel]. Its "Open <app>" button starts the installed app with [launchEnvironment] added.
 */
public class ManualCase internal constructor(
    public val id: String,
    public val reason: ManualReason,
    public val data: List<String>,
    public val title: String,
    public val steps: String,
    public val expected: String,
    public val passLabel: String,
    public val failLabel: String,
    public val launchEnvironment: Map<String, String>
)

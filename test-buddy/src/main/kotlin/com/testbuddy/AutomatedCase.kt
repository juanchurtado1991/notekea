package com.testbuddy

import kotlin.time.Duration

/** A case the program checks: [body] drives the app as a person would and fails when the app does not do [title]. */
public class AutomatedCase internal constructor(
    public val id: String,
    public val title: String,
    public val timeout: Duration,
    internal val body: CaseScope.() -> Unit
)

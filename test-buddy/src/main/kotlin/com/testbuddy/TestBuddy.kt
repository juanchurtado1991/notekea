package com.testbuddy

/**
 * Declares a host app's suite: how to show the app ([SuiteBuilder.launch]), its test data, its automated and manual
 * cases and what to back up. The same suite runs in CI (`TestBuddyJUnit`) and in the Self-Test app.
 */
public fun testBuddy(
    appName: String,
    version: String,
    build: SuiteBuilder.() -> Unit
): Suite = SuiteBuilder(appName = appName, version = version).apply(build).build()

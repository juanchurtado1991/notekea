package com.testbuddy

/**
 * Declares a host app's suite: how to show the app ([SuiteBuilder.launch]), its test data, its automated and manual
 * cases and what to back up. The manager speaks [TesterLanguage], the language the manual cases are written in. The
 * same suite runs in CI (`TestBuddyJUnit`) and in the Auto-Test app.
 */
public fun testBuddy(
    appName: String,
    version: String,
    language: TesterLanguage = TesterLanguage.ENGLISH,
    build: SuiteBuilder.() -> Unit
): Suite = SuiteBuilder(
    appName = appName,
    version = version,
    language = language
).apply(build).build()

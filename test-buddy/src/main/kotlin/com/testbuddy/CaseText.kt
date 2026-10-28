package com.testbuddy

/**
 * A text shown to a tester, in their language: [resolve] gets an ISO 639 code ("en", "es") and falls back to English,
 * the source language. Hosts usually back it with their string resources.
 */
public fun interface CaseText {
    public suspend fun resolve(language: String): String
}

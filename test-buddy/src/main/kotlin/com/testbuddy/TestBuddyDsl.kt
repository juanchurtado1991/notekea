package com.testbuddy

/** Marks Test Buddy's builders, so a case's body cannot call the suite's builder by mistake. */
@DslMarker
public annotation class TestBuddyDsl

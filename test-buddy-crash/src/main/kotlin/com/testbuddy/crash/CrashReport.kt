package com.testbuddy.crash

/** A crash as kept: its report's [text], and the [path] of its file when it could be written. */
public data class CrashReport(
    val text: String,
    val path: String?
)

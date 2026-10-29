package com.testbuddy.app

import com.testbuddy.CaseResult

/** How an automatic case stands in the list. */
internal enum class CaseStatus {
    WAITING,
    RUNNING,
    PASSED,
    FAILED,
    SKIPPED;

    companion object {
        fun of(
            id: String,
            state: SelfTestState
        ): CaseStatus = when (state.results[id]) {
            is CaseResult.Passed -> PASSED
            is CaseResult.Failed -> FAILED
            is CaseResult.Skipped -> SKIPPED
            null -> if (id == state.runningId) RUNNING else WAITING
        }
    }
}

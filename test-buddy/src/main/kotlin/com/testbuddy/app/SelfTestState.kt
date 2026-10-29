package com.testbuddy.app

import com.testbuddy.CaseResult
import com.testbuddy.ManualAnswer

/**
 * What the Self-Test window shows: the [step], the automatic [results] by case ID and the case [runningId] runs now,
 * the manual case at [manualIndex] and the [answers] given, then the [report] and whether the tester's machine [isClean].
 */
internal data class SelfTestState(
    val step: SelfTestStep = SelfTestStep.AUTOMATIC,
    val results: Map<String, CaseResult> = emptyMap(),
    val runningId: String? = null,
    val isRunning: Boolean = false,
    val manualIndex: Int = 0,
    val answers: Map<String, ManualAnswer> = emptyMap(),
    val report: String = "",
    val isClean: Boolean = false
)

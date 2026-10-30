package com.testbuddy.app

import com.testbuddy.CaseResult
import com.testbuddy.ManualAnswer

/**
 * What the manager shows: the [tab]; the automatic [results] by case ID, whether a run [isRunning], the case it runs
 * ([runningId]) with the [runningSteps] it took so far and when the run started ([startedAt], epoch milliseconds);
 * the manual case at [manualIndex] and its step at [manualStep] (its step count means the result is asked), the
 * [answers] given; and the [report].
 */
internal data class ManagerState(
    val tab: ManagerTab = ManagerTab.AUTOMATIC,
    val results: Map<String, CaseResult> = emptyMap(),
    val isRunning: Boolean = false,
    val runningId: String? = null,
    val runningSteps: List<String> = emptyList(),
    val startedAt: Long? = null,
    val manualIndex: Int = 0,
    val manualStep: Int = 0,
    val answers: Map<String, ManualAnswer> = emptyMap(),
    val report: String = ""
)

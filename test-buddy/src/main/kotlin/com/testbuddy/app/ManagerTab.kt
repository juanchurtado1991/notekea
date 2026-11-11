package com.testbuddy.app

/** The manager's three tabs, in order. */
internal enum class ManagerTab {
    AUTOMATIC,
    MANUAL,
    REPORT;

    fun label(texts: TesterTexts): String = when (this) {
        AUTOMATIC -> texts.tabAutomatic
        MANUAL -> texts.tabManual
        REPORT -> texts.tabReport
    }
}

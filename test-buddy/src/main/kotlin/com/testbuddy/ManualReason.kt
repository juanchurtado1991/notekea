package com.testbuddy

/** Why a person checks a case instead of the program; shown as `Manual (<label>)` in the case list. */
public enum class ManualReason(public val label: String) {
    /** A person must judge how it looks or feels. */
    EYES(label = "eyes"),

    /** It needs something outside the app: a native dialog, the system's settings, another app. */
    SYSTEM(label = "system"),

    /** It only exists in the installed app: its launcher entry, its installer, its process. */
    PACKAGED(label = "packaged"),

    /** Automating it would cost far more than the case is worth. */
    COSTLY(label = "costly")
}

package app.notekea.autotest

import com.testbuddy.AutomatedCase

/** How many parts the automated cases are split into, one test class each (`NotekeaSuitePart1Test`…). */
object NotekeaSuiteParts {
    const val COUNT = 4

    /**
     * Whether [case] asks a chat model: those run one after another in part 1, since four apps loading the model at
     * once ran out of GPU memory (2026-11-13, `kIOGPUCommandBufferCallbackErrorOutOfMemory`).
     */
    fun asksModel(case: AutomatedCase): Boolean =
        case.precondition === ChatModels.any || case.precondition === ChatModels.medium
}

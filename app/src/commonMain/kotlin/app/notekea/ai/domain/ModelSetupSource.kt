package app.notekea.ai.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.LlmError

/**
 * Where the engine learns how to load the model in use: [LlmError.NoModel] while no chat model is installed and
 * chosen, [LlmError.NotEnoughMemory] when the one chosen does not fit this machine.
 */
fun interface ModelSetupSource {
    suspend fun current(): Outcome<ModelSetup, LlmError>
}

package app.notekea.llm.domain

import kotlinx.coroutines.flow.Flow

/**
 * Local language model for chat generation (`ROADMAP.md` 3.2). Embeddings come from a smaller model of their own
 * ([app.notekea.core.embedding.domain.TextEmbedder], 2.2). Platform implementations live outside `commonMain`
 * (llama.cpp on desktop).
 */
interface LlmEngine {
    fun generate(
        prompt: String
    ): Flow<String>
}

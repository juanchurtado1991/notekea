package app.notekea.ai.data

import app.notekea.ai.domain.ModelSetup
import kotlinx.coroutines.sync.Semaphore
import net.ladenthin.llama.LlamaModel

/** The chat model and the reranker loaded as [setup] says, with one permit of [slots] per reply running. */
class LoadedLlama(
    val setup: ModelSetup,
    val chat: LlamaModel,
    val reranker: LlamaModel
) : AutoCloseable {
    val slots = Semaphore(permits = setup.slots)

    override fun close() {
        chat.close()
        reranker.close()
    }
}

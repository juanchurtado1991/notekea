package app.notekea.ai.data

import app.notekea.ai.domain.ModelSetup
import net.ladenthin.llama.LlamaModel

/** The chat model and the reranker loaded as [setup] says, with one permit of [slots] per reply running. */
class LoadedLlama(
    val setup: ModelSetup,
    val chat: LlamaModel,
    val reranker: LlamaModel
) : AutoCloseable {
    /** The context the chat model's calls share: every slot's tokens as one room. */
    val room = TokenRoom(
        total = setup.contextTokens * setup.slots,
        slots = setup.slots
    )

    override fun close() {
        chat.close()
        reranker.close()
    }
}

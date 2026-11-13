package app.notekea

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.core.embedding.data.LlamaTextEmbedder
import org.koin.core.Koin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The models leave memory before the process ends, however it ends (the window closed, Cmd+Q, a session ending): none
 * of those ways stopped Koin, so a chat model used in the last five minutes was still on Metal when the native
 * library's own exit ran, which aborts on that (the crash report at quit; code review of 2026-11-13).
 */
internal object ModelsAtExit {
    // Longer than the wait for the note being typed to be saved (`WindowQuit`), which a halt here would cut.
    private val WAIT = 6.seconds

    fun install(koin: Koin) {
        Runtime
            .getRuntime()
            .addShutdownHook(
                Thread {
                    release(
                        engine = koin.getOrNull(),
                        embedder = koin.getOrNull()
                    )
                }
            )
    }

    /**
     * Frees both models, waiting [within] for a reply still being written; when one is still there, [halt] ends the
     * process without the native library's exit, which is the only clean way left.
     */
    fun release(
        engine: LlamaLlmEngine?,
        embedder: LlamaTextEmbedder?,
        within: Duration = WAIT,
        halt: () -> Unit = { Runtime.getRuntime().halt(0) }
    ) {
        val isChatFreed = engine?.freed(within = within) ?: true
        val isEmbedderFreed = embedder?.freed(within = within) ?: true
        if (!isChatFreed || !isEmbedderFreed) halt()
    }
}

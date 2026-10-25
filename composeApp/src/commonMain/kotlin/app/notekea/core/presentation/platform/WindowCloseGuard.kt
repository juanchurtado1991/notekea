package app.notekea.core.presentation.platform

import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * Work that must finish before the window closes (the editor saving the note being typed). The window asks [beforeClose]
 * when the user closes it and exits afterwards; screens register a handler while they are shown, like a
 * [ShortcutDispatcher] handler. Each handler gets [HANDLER_TIMEOUT], so a stuck disk never keeps the app from closing.
 * Used on the UI thread only.
 */
class WindowCloseGuard {
    private val handlers = mutableListOf<suspend () -> Unit>()

    /** Runs every registered handler, the most recent first. */
    suspend fun beforeClose() {
        handlers.asReversed().toList().forEach { handler -> withTimeoutOrNull(timeout = HANDLER_TIMEOUT) { handler() } }
    }

    /** Adds [handler] and returns the function that removes it. */
    fun register(
        handler: suspend () -> Unit
    ): () -> Unit {
        handlers += handler
        return { handlers -= handler }
    }

    companion object {
        val HANDLER_TIMEOUT = 3.seconds
    }
}
